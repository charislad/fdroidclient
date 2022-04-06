package org.fdroid.database

import androidx.core.os.LocaleListCompat
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import org.fdroid.database.VersionedStringType.PERMISSION
import org.fdroid.database.VersionedStringType.PERMISSION_SDK_23
import org.fdroid.index.v2.FeatureV2
import org.fdroid.index.v2.FileV1
import org.fdroid.index.v2.FileV2
import org.fdroid.index.v2.LocalizedTextV2
import org.fdroid.index.v2.ManifestV2
import org.fdroid.index.v2.PackageVersionV2
import org.fdroid.index.v2.PermissionV2
import org.fdroid.index.v2.SignatureV2
import org.fdroid.index.v2.UsesSdkV2

@Entity(
    primaryKeys = ["repoId", "packageId", "versionId"],
    foreignKeys = [ForeignKey(
        entity = AppMetadata::class,
        parentColumns = ["repoId", "packageId"],
        childColumns = ["repoId", "packageId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class Version(
    val repoId: Long,
    val packageId: String,
    val versionId: String,
    val added: Long,
    @Embedded(prefix = "file_") val file: FileV1,
    @Embedded(prefix = "src_") val src: FileV2? = null,
    @Embedded(prefix = "manifest_") val manifest: AppManifest,
    val releaseChannels: List<String>? = emptyList(),
    val antiFeatures: Map<String, LocalizedTextV2>? = null,
    val whatsNew: LocalizedTextV2? = null,
    val isCompatible: Boolean,
) {
    fun toAppVersion(versionedStrings: List<VersionedString>) = AppVersion(
        version = this,
        usesPermission = versionedStrings.getPermissions(this),
        usesPermissionSdk23 = versionedStrings.getPermissionsSdk23(this),
    )
}

fun PackageVersionV2.toVersion(
    repoId: Long,
    packageId: String,
    versionId: String,
    isCompatible: Boolean,
) = Version(
    repoId = repoId,
    packageId = packageId,
    versionId = versionId,
    added = added,
    file = file,
    src = src,
    manifest = manifest.toManifest(),
    releaseChannels = releaseChannels,
    antiFeatures = antiFeatures,
    whatsNew = whatsNew,
    isCompatible = isCompatible,
)

data class AppVersion(
    val version: Version,
    val usesPermission: List<PermissionV2>? = null,
    val usesPermissionSdk23: List<PermissionV2>? = null,
) {
    val packageId get() = version.packageId
    val featureNames get() = version.manifest.features?.toTypedArray() ?: emptyArray()
    val nativeCode get() = version.manifest.nativecode?.toTypedArray() ?: emptyArray()
    val antiFeatureNames: Array<String>
        get() {
            return version.antiFeatures?.map { it.key }?.toTypedArray() ?: emptyArray()
        }

    fun getWhatsNew(localeList: LocaleListCompat) = version.whatsNew.getBestLocale(localeList)
}

data class AppManifest(
    val versionName: String,
    val versionCode: Long,
    @Embedded(prefix = "usesSdk_") val usesSdk: UsesSdkV2? = null,
    val maxSdkVersion: Int? = null,
    @Embedded(prefix = "signer_") val signer: SignatureV2? = null,
    val nativecode: List<String>? = emptyList(),
    val features: List<String>? = emptyList(),
) {
    internal fun toManifestV2(): ManifestV2 = ManifestV2(
        versionName = versionName,
        versionCode = versionCode,
        usesSdk = usesSdk,
        maxSdkVersion = maxSdkVersion,
        signer = signer,
        nativeCode = nativecode ?: emptyList(),
        features = features?.map { FeatureV2(it) } ?: emptyList(),
    )
}

fun ManifestV2.toManifest() = AppManifest(
    versionName = versionName,
    versionCode = versionCode,
    usesSdk = usesSdk,
    maxSdkVersion = maxSdkVersion,
    signer = signer,
    nativecode = nativeCode,
    features = features.map { it.name },
)

enum class VersionedStringType {
    PERMISSION,
    PERMISSION_SDK_23,
}

@Entity(
    primaryKeys = ["repoId", "packageId", "versionId", "type", "name"],
    foreignKeys = [ForeignKey(
        entity = Version::class,
        parentColumns = ["repoId", "packageId", "versionId"],
        childColumns = ["repoId", "packageId", "versionId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class VersionedString(
    val repoId: Long,
    val packageId: String,
    val versionId: String,
    val type: VersionedStringType,
    val name: String,
    val version: Int? = null,
)

fun List<PermissionV2>.toVersionedString(
    version: Version,
    type: VersionedStringType,
) = map { permission ->
    VersionedString(
        repoId = version.repoId,
        packageId = version.packageId,
        versionId = version.versionId,
        type = type,
        name = permission.name,
        version = permission.maxSdkVersion,
    )
}

fun ManifestV2.getVersionedStrings(version: Version): List<VersionedString> {
    return usesPermission.toVersionedString(version, PERMISSION) +
        usesPermissionSdk23.toVersionedString(version, PERMISSION_SDK_23)
}

fun List<VersionedString>.getPermissions(version: Version) = mapNotNull { v ->
    v.map(version, PERMISSION) {
        PermissionV2(
            name = v.name,
            maxSdkVersion = v.version,
        )
    }
}

fun List<VersionedString>.getPermissionsSdk23(version: Version) = mapNotNull { v ->
    v.map(version, PERMISSION_SDK_23) {
        PermissionV2(
            name = v.name,
            maxSdkVersion = v.version,
        )
    }
}

private fun <T> VersionedString.map(
    v: Version,
    wantedType: VersionedStringType,
    factory: () -> T,
): T? {
    return if (repoId != v.repoId || packageId != v.packageId || versionId != v.versionId ||
        type != wantedType
    ) null
    else factory()
}