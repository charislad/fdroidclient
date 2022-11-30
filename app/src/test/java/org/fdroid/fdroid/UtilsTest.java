
package org.fdroid.fdroid;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.database.Cursor;

import org.fdroid.fdroid.data.AppProvider;
import org.fdroid.fdroid.data.Schema;
import org.fdroid.fdroid.views.AppDetailsRecyclerViewAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import androidx.loader.content.CursorLoader;
import androidx.test.core.app.ApplicationProvider;
import vendored.org.apache.commons.codec.digest.DigestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static vendored.org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_256;

/**
 * @see <a href="https://gitlab.com/fdroid/fdroidclient/-/merge_requests/1089#note_822501322">forced to vendor Apache Commons Codec</a>
 */
@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("LineLength")
public class UtilsTest {

    String fdroidFingerprint = "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB";
    String fdroidPubkey = "3082035e30820246a00302010202044c49cd00300d06092a864886f70d01010505003071310b300906035504061302554b3110300e06035504081307556e6b6e6f776e3111300f0603550407130857657468657262793110300e060355040a1307556e6b6e6f776e3110300e060355040b1307556e6b6e6f776e311930170603550403131043696172616e2047756c746e69656b73301e170d3130303732333137313032345a170d3337313230383137313032345a3071310b300906035504061302554b3110300e06035504081307556e6b6e6f776e3111300f0603550407130857657468657262793110300e060355040a1307556e6b6e6f776e3110300e060355040b1307556e6b6e6f776e311930170603550403131043696172616e2047756c746e69656b7330820122300d06092a864886f70d01010105000382010f003082010a028201010096d075e47c014e7822c89fd67f795d23203e2a8843f53ba4e6b1bf5f2fd0e225938267cfcae7fbf4fe596346afbaf4070fdb91f66fbcdf2348a3d92430502824f80517b156fab00809bdc8e631bfa9afd42d9045ab5fd6d28d9e140afc1300917b19b7c6c4df4a494cf1f7cb4a63c80d734265d735af9e4f09455f427aa65a53563f87b336ca2c19d244fcbba617ba0b19e56ed34afe0b253ab91e2fdb1271f1b9e3c3232027ed8862a112f0706e234cf236914b939bcf959821ecb2a6c18057e070de3428046d94b175e1d89bd795e535499a091f5bc65a79d539a8d43891ec504058acb28c08393b5718b57600a211e803f4a634e5c57f25b9b8c4422c6fd90203010001300d06092a864886f70d0101050500038201010008e4ef699e9807677ff56753da73efb2390d5ae2c17e4db691d5df7a7b60fc071ae509c5414be7d5da74df2811e83d3668c4a0b1abc84b9fa7d96b4cdf30bba68517ad2a93e233b042972ac0553a4801c9ebe07bf57ebe9a3b3d6d663965260e50f3b8f46db0531761e60340a2bddc3426098397fda54044a17e5244549f9869b460ca5e6e216b6f6a2db0580b480ca2afe6ec6b46eedacfa4aa45038809ece0c5978653d6c85f678e7f5a2156d1bedd8117751e64a4b0dcd140f3040b021821a8d93aed8d01ba36db6c82372211fed714d9a32607038cdfd565bd529ffc637212aaa2c224ef22b603eccefb5bf1e085c191d4b24fe742b17ab3f55d4e6f05ef";

    String gpRepoFingerprint = "59050C8155DCA377F23D5A15B77D3713400CDBD8B42FBFBE0E3F38096E68CECE";
    String gpRepoPubkey = "308203c5308202ada00302010202047b7cf549300d06092a864886f70d01010b0500308192310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b311d301b060355040a131454686520477561726469616e2050726f6a656374311f301d060355040b1316477561726469616e20462d44726f6964204275696c64311d301b06035504031314677561726469616e70726f6a6563742e696e666f301e170d3132313032393130323530305a170d3430303331363130323530305a308192310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b311d301b060355040a131454686520477561726469616e2050726f6a656374311f301d060355040b1316477561726469616e20462d44726f6964204275696c64311d301b06035504031314677561726469616e70726f6a6563742e696e666f30820122300d06092a864886f70d01010105000382010f003082010a0282010100b7f1f635fa3fce1a8042aaa960c2dc557e4ad2c082e5787488cba587fd26207cf59507919fc4dcebda5c8c0959d14146d0445593aa6c29dc639570b71712451fd5c231b0c9f5f0bec380503a1c2a3bc00048bc5db682915afa54d1ecf67b45e1e05c0934b3037a33d3a565899131f27a72c03a5de93df17a2376cc3107f03ee9d124c474dfab30d4053e8f39f292e2dcb6cc131bce12a0c5fc307985195d256bf1d7a2703d67c14bf18ed6b772bb847370b20335810e337c064fef7e2795a524c664a853cd46accb8494f865164dabfb698fa8318236432758bc40d52db00d5ce07fe2210dc06cd95298b4f09e6c9b7b7af61c1d62ea43ea36a2331e7b2d4e250203010001a321301f301d0603551d0e0416041404d763e981cf3a295b94a790d8536a783097232b300d06092a864886f70d01010b05000382010100654e6484ff032c54fed1d96d3c8e731302be9dbd7bb4fe635f2dac05b69f3ecbb5acb7c9fe405e2a066567a8f5c2beb8b199b5a4d5bb1b435cf02df026d4fb4edd9d8849078f085b00950083052d57467d65c6eebd98f037cff9b148d621cf8819c4f7dc1459bf8fc5c7d76f901495a7caf35d1e5c106e1d50610c4920c3c1b50adcfbd4ad83ce7353cdea7d856bba0419c224f89a2f3ebc203d20eb6247711ad2b55fd4737936dc42ced7a047cbbd24012079204a2883b6d55d5d5b66d9fd82fb51fca9a5db5fad9af8564cb380ff30ae8263dbbf01b46e01313f53279673daa3f893380285646b244359203e7eecde94ae141b7dfa8e6499bb8e7e0b25ab85";

    String gpTest0Fingerprint = "C4DC0B2AB5AB58F0CDBF97FF903CF12415F468D90B11877803BC172D31012B2E";
    String gpTest0Pubkey = "308204f3308202dba003020102020436aac0dc300d06092a864886f70d01010b0500302a3110300e060355040b1307462d44726f6964311630140603550403130d70616c6174736368696e6b656e301e170d3133313130353232353534325a170d3133313130363232353534325a302a3110300e060355040b1307462d44726f6964311630140603550403130d70616c6174736368696e6b656e30820222300d06092a864886f70d01010105000382020f003082020a0282020100b1f3cd3db9207f80e9d854159d40a15344bfcc377fba61983d1ac843e52e2fc1a81d96325174328f77dbe382b2b239567d50ad2e1fea13f1272b0370693acd03b9aef3e5a908118065f21193735552c123a9f59f99c2822b7bba7082c72649e17666ac70d332f1c7cf20830373c86f11d2f80a2aa0307c3b526b8769b69371555540f246ca892db4b51226788bb3b869284254266f3ccb1d7b5b08a2cf398f53877b09da0f1cc922ecc928c477660979d07998b29678feaea9b5c93d3a12f89f695eeda766280df22b688e1da15d979845a81c81f9d1252e2e5fd415df2eb0f28cb339a9d9bc13ec1a059333ca766a0982464f8d9a67397f066b3926aa5ac6f2216962da5705d2b9723353ac3b670f5ab4d365cde4e5d0375ca52e7e8c151dd90eda0025be09feae4c94c59608243b45f0527ad8d46e0a0bc97ac27870af53c0550706502ecfa56a30d7442012e6115ada79243481b759319def848199df423c9664574d8d8a7f8949e9f3549e8695fa0b02eab1dc8e30c6432d159076ceb91b709bd848f4e5d74a0880c1ead0534b1f8a354edd05a6d7b44f9a566f9e419bab6834ff2f2d2a54c797b407ccb2d4648247e69b2b85186f9ebd087879a580be281b73f46975e5c94b5a935adf019d6d56992742ebb23865f94a14ed17fc7fb0fbea43eb686760298ae98b197ac8da2ec0b61be240b6f2a574208da9e0fd9e14d90203010001a321301f301d0603551d0e04160414282e3362786f92645dd7809905166e473bbfc722300d06092a864886f70d01010b05000382020100295efaa7d0e985b408a7c6f2891cae1fa7b6338774eee624edd838c0fbaadc755d140ed6007b91e662434010659a4a5597709e23828a1a5e9846b4369ee8fcef10b85fc64db7726aee8c8d93753d4828250323ebdb768ed9958f4c2c61eb48d2329a0196a47898662ed9418e5ba223c4c1e285e94bfe0f5d5b4813b9d0b6b49d304a79879698d320e1ff5e36be441f1dcda5715d4644825d669b15de2765d285253231fbe052360426fe976af404381909043cfe8e7a537275dc75f367235eb0fc357884ea36f00cdb21fbc75ca2ac9c53adc202456e40d0e950af09c4f5de3d876f43fda7880be4800ff2635f681c19a5b8f1cd68319e78f5ff8e29f5225db849f03d473926aa2d492df970cbcba266211003e7c84f5852ea089b62679acd6243e56b18384596443c379effa1419027345bb929a46193c5c1f6274c83f14a8720189ab178945ef56a99fb16ac8aedb6d8b9ec10bd1f6935b189aa9470947d909bf8d0630e8b189696a79e9560631fa79cc22cddc17594c2c86e03fa7102811154d10aa2ff631732c491969b8a356632eabcf22596db4eb011cfaf426ec972967e2c5f426fa1f3e9d8c1d90fbb7665660d02456d9b7c1fa6bb68b0d53c29c6ef4e7c81f91c1819f754a53a03124a36b539cde945287c5be8817244c1548c17ff671f729545dc9155c94f01ceb620333f10000acbeba866cb242155daa76a5169";

    String gpTest1Fingerprint = "C63AED1AC79D37C7B0474472AC6EFA6C3AB2B11A767A4F42CF360FA5496E3C50";
    String gpTest1Pubkey = "3082039a30820282020900aa6887be1ec84bde300d06092a864886f70d010105050030818e310b30090603550406130255533111300f06035504080c084e657720596f726b311e301c060355040a0c15477561726469616e2050726f6a65637420546573743122302006035504030c19746573742e677561726469616e70726f6a6563742e696e666f3128302606092a864886f70d01090116197465737440677561726469616e70726f6a6563742e696e666f301e170d3134303332383230343132365a170d3431303831323230343132365a30818e310b30090603550406130255533111300f06035504080c084e657720596f726b311e301c060355040a0c15477561726469616e2050726f6a65637420546573743122302006035504030c19746573742e677561726469616e70726f6a6563742e696e666f3128302606092a864886f70d01090116197465737440677561726469616e70726f6a6563742e696e666f30820122300d06092a864886f70d01010105000382010f003082010a02820101009f4895a4a160d14e9de49dd61ac9434715c2aea25a9de75f0361e3f9bd77306cff7a8f508f9a9edc31dfb5b3aa2571e22b1711c08f0616892fa4efdf94321ec93211486b314bcf27385f670492683a0e50f5a022ede2bfc00c69b14e8c8678f313d6d280feb9c53445f087fa9d12a31392ca63d75351587e3cd2337fbf95fd7c2a9322883d74f18680165a697d4a1a4fa3bd835bd45f00561447350af4ec6b6740c0ae7950ff53c386a2efc43a280e4270912d20eb464761799fdbbae50dd0df01f9b25673499029a2e869203e7d63e7ca98826dabf856c965f472de691ddc77f6ed8db468684baf76f7f1cdf7fc3a07109ad8aea8e332a807bedbb8143bbe230203010001300d06092a864886f70d010105050003820101005284015baba5eb092a3c681634b46b9f59a0dbb651c89ca65af730bfeb22726e048194cbd54fb4242f5ec8e514e26dd8887cbcb431f3f2eb224780b6a2204e614d705aed4bd66e153c216d35e1dc1e38e226566af74bb229a2416ea6ffb388d6f64a68386332f34f50d48b630541e2871030bd27d90a1688f46bff4e9707059cd22e56820a4a3d01f9a91b442f6adf0776d9f73533a2dcd7214305491414dbc7c734166cd833e227f9bd8a82b3d464c662c71a07703fb14de0564cad1d3851e35cc9a04ce36fde2abf8d8d9dec07752e535f35aabc3632d6d2106086477e346efebb0d4bec7afc461d7ab7f96200c2dadb2da41d09342aa2fa9ab94ab92d2053";

    // this pair has one digit missing from the pubkey
    String pubkeyShortByOneFingerprint = "C63AED1AC79D37C7B0474472AC6EFA6C3AB2B11A767A4F42CF360FA5496E3C50";
    String pubkeyShortByOnePubkey = "3082039a30820282020900aa6887be1ec84bde300d06092a86488f70d010105050030818e310b30090603550406130255533111300f06035504080c084e657720596f726b311e301c060355040a0c15477561726469616e2050726f6a65637420546573743122302006035504030c19746573742e677561726469616e70726f6a6563742e696e666f3128302606092a864886f70d01090116197465737440677561726469616e70726f6a6563742e696e666f301e170d3134303332383230343132365a170d3431303831323230343132365a30818e310b30090603550406130255533111300f06035504080c084e657720596f726b311e301c060355040a0c15477561726469616e2050726f6a65637420546573743122302006035504030c19746573742e677561726469616e70726f6a6563742e696e666f3128302606092a864886f70d01090116197465737440677561726469616e70726f6a6563742e696e666f30820122300d06092a864886f70d01010105000382010f003082010a02820101009f4895a4a160d14e9de49dd61ac9434715c2aea25a9de75f0361e3f9bd77306cff7a8f508f9a9edc31dfb5b3aa2571e22b1711c08f0616892fa4efdf94321ec93211486b314bcf27385f670492683a0e50f5a022ede2bfc00c69b14e8c8678f313d6d280feb9c53445f087fa9d12a31392ca63d75351587e3cd2337fbf95fd7c2a9322883d74f18680165a697d4a1a4fa3bd835bd45f00561447350af4ec6b6740c0ae7950ff53c386a2efc43a280e4270912d20eb464761799fdbbae50dd0df01f9b25673499029a2e869203e7d63e7ca98826dabf856c965f472de691ddc77f6ed8db468684baf76f7f1cdf7fc3a07109ad8aea8e332a807bedbb8143bbe230203010001300d06092a864886f70d010105050003820101005284015baba5eb092a3c681634b46b9f59a0dbb651c89ca65af730bfeb22726e048194cbd54fb4242f5ec8e514e26dd8887cbcb431f3f2eb224780b6a2204e614d705aed4bd66e153c216d35e1dc1e38e226566af74bb229a2416ea6ffb388d6f64a68386332f34f50d48b630541e2871030bd27d90a1688f46bff4e9707059cd22e56820a4a3d01f9a91b442f6adf0776d9f73533a2dcd7214305491414dbc7c734166cd833e227f9bd8a82b3d464c662c71a07703fb14de0564cad1d3851e35cc9a04ce36fde2abf8d8d9dec07752e535f35aabc3632d6d2106086477e346efebb0d4bec7afc461d7ab7f96200c2dadb2da41d09342aa2fa9ab94ab92d2053";

    // this pair has one digit missing from the fingerprint
    String fingerprintShortByOneFingerprint = "C63AED1AC79D37C7B047442AC6EFA6C3AB2B11A767A4F42CF360FA5496E3C50";
    String fingerprintShortByOnePubkey = "3082039a30820282020900aa6887be1ec84bde300d06092a864886f70d010105050030818e310b30090603550406130255533111300f06035504080c084e657720596f726b311e301c060355040a0c15477561726469616e2050726f6a65637420546573743122302006035504030c19746573742e677561726469616e70726f6a6563742e696e666f3128302606092a864886f70d01090116197465737440677561726469616e70726f6a6563742e696e666f301e170d3134303332383230343132365a170d3431303831323230343132365a30818e310b30090603550406130255533111300f06035504080c084e657720596f726b311e301c060355040a0c15477561726469616e2050726f6a65637420546573743122302006035504030c19746573742e677561726469616e70726f6a6563742e696e666f3128302606092a864886f70d01090116197465737440677561726469616e70726f6a6563742e696e666f30820122300d06092a864886f70d01010105000382010f003082010a02820101009f4895a4a160d14e9de49dd61ac9434715c2aea25a9de75f0361e3f9bd77306cff7a8f508f9a9edc31dfb5b3aa2571e22b1711c08f0616892fa4efdf94321ec93211486b314bcf27385f670492683a0e50f5a022ede2bfc00c69b14e8c8678f313d6d280feb9c53445f087fa9d12a31392ca63d75351587e3cd2337fbf95fd7c2a9322883d74f18680165a697d4a1a4fa3bd835bd45f00561447350af4ec6b6740c0ae7950ff53c386a2efc43a280e4270912d20eb464761799fdbbae50dd0df01f9b25673499029a2e869203e7d63e7ca98826dabf856c965f472de691ddc77f6ed8db468684baf76f7f1cdf7fc3a07109ad8aea8e332a807bedbb8143bbe230203010001300d06092a864886f70d010105050003820101005284015baba5eb092a3c681634b46b9f59a0dbb651c89ca65af730bfeb22726e048194cbd54fb4242f5ec8e514e26dd8887cbcb431f3f2eb224780b6a2204e614d705aed4bd66e153c216d35e1dc1e38e226566af74bb229a2416ea6ffb388d6f64a68386332f34f50d48b630541e2871030bd27d90a1688f46bff4e9707059cd22e56820a4a3d01f9a91b442f6adf0776d9f73533a2dcd7214305491414dbc7c734166cd833e227f9bd8a82b3d464c662c71a07703fb14de0564cad1d3851e35cc9a04ce36fde2abf8d8d9dec07752e535f35aabc3632d6d2106086477e346efebb0d4bec7afc461d7ab7f96200c2dadb2da41d09342aa2fa9ab94ab92d2053";

    // this pair has one digit added to the pubkey
    String pubkeyLongByOneFingerprint = "59050C8155DCA377F23D5A15B77D3713400CDBD8B42FBFBE0E3F38096E68CECE";
    String pubkeyLongByOnePubkey = "308203c5308202ada00302010202047b7cf5493000d06092a864886f70d01010b0500308192310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b311d301b060355040a131454686520477561726469616e2050726f6a656374311f301d060355040b1316477561726469616e20462d44726f6964204275696c64311d301b06035504031314677561726469616e70726f6a6563742e696e666f301e170d3132313032393130323530305a170d3430303331363130323530305a308192310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b311d301b060355040a131454686520477561726469616e2050726f6a656374311f301d060355040b1316477561726469616e20462d44726f6964204275696c64311d301b06035504031314677561726469616e70726f6a6563742e696e666f30820122300d06092a864886f70d01010105000382010f003082010a0282010100b7f1f635fa3fce1a8042aaa960c2dc557e4ad2c082e5787488cba587fd26207cf59507919fc4dcebda5c8c0959d14146d0445593aa6c29dc639570b71712451fd5c231b0c9f5f0bec380503a1c2a3bc00048bc5db682915afa54d1ecf67b45e1e05c0934b3037a33d3a565899131f27a72c03a5de93df17a2376cc3107f03ee9d124c474dfab30d4053e8f39f292e2dcb6cc131bce12a0c5fc307985195d256bf1d7a2703d67c14bf18ed6b772bb847370b20335810e337c064fef7e2795a524c664a853cd46accb8494f865164dabfb698fa8318236432758bc40d52db00d5ce07fe2210dc06cd95298b4f09e6c9b7b7af61c1d62ea43ea36a2331e7b2d4e250203010001a321301f301d0603551d0e0416041404d763e981cf3a295b94a790d8536a783097232b300d06092a864886f70d01010b05000382010100654e6484ff032c54fed1d96d3c8e731302be9dbd7bb4fe635f2dac05b69f3ecbb5acb7c9fe405e2a066567a8f5c2beb8b199b5a4d5bb1b435cf02df026d4fb4edd9d8849078f085b00950083052d57467d65c6eebd98f037cff9b148d621cf8819c4f7dc1459bf8fc5c7d76f901495a7caf35d1e5c106e1d50610c4920c3c1b50adcfbd4ad83ce7353cdea7d856bba0419c224f89a2f3ebc203d20eb6247711ad2b55fd4737936dc42ced7a047cbbd24012079204a2883b6d55d5d5b66d9fd82fb51fca9a5db5fad9af8564cb380ff30ae8263dbbf01b46e01313f53279673daa3f893380285646b244359203e7eecde94ae141b7dfa8e6499bb8e7e0b25ab85";

    // this pair has one digit added to the fingerprint
    String fingerprintLongByOneFingerprint = "59050C8155DCA377F23D5A15B77D37134000CDBD8B42FBFBE0E3F38096E68CECE";
    String fingerprintLongByOnePubkey = "308203c5308202ada00302010202047b7cf549300d06092a864886f70d01010b0500308192310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b311d301b060355040a131454686520477561726469616e2050726f6a656374311f301d060355040b1316477561726469616e20462d44726f6964204275696c64311d301b06035504031314677561726469616e70726f6a6563742e696e666f301e170d3132313032393130323530305a170d3430303331363130323530305a308192310b30090603550406130255533111300f060355040813084e657720596f726b3111300f060355040713084e657720596f726b311d301b060355040a131454686520477561726469616e2050726f6a656374311f301d060355040b1316477561726469616e20462d44726f6964204275696c64311d301b06035504031314677561726469616e70726f6a6563742e696e666f30820122300d06092a864886f70d01010105000382010f003082010a0282010100b7f1f635fa3fce1a8042aaa960c2dc557e4ad2c082e5787488cba587fd26207cf59507919fc4dcebda5c8c0959d14146d0445593aa6c29dc639570b71712451fd5c231b0c9f5f0bec380503a1c2a3bc00048bc5db682915afa54d1ecf67b45e1e05c0934b3037a33d3a565899131f27a72c03a5de93df17a2376cc3107f03ee9d124c474dfab30d4053e8f39f292e2dcb6cc131bce12a0c5fc307985195d256bf1d7a2703d67c14bf18ed6b772bb847370b20335810e337c064fef7e2795a524c664a853cd46accb8494f865164dabfb698fa8318236432758bc40d52db00d5ce07fe2210dc06cd95298b4f09e6c9b7b7af61c1d62ea43ea36a2331e7b2d4e250203010001a321301f301d0603551d0e0416041404d763e981cf3a295b94a790d8536a783097232b300d06092a864886f70d01010b05000382010100654e6484ff032c54fed1d96d3c8e731302be9dbd7bb4fe635f2dac05b69f3ecbb5acb7c9fe405e2a066567a8f5c2beb8b199b5a4d5bb1b435cf02df026d4fb4edd9d8849078f085b00950083052d57467d65c6eebd98f037cff9b148d621cf8819c4f7dc1459bf8fc5c7d76f901495a7caf35d1e5c106e1d50610c4920c3c1b50adcfbd4ad83ce7353cdea7d856bba0419c224f89a2f3ebc203d20eb6247711ad2b55fd4737936dc42ced7a047cbbd24012079204a2883b6d55d5d5b66d9fd82fb51fca9a5db5fad9af8564cb380ff30ae8263dbbf01b46e01313f53279673daa3f893380285646b244359203e7eecde94ae141b7dfa8e6499bb8e7e0b25ab85";

    Context context;
    Cursor cursor;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        Preferences.setupForTests(context);
        cursor = null;
    }

    @After
    public void tearDown() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test
    public void trailingNewLines() {
        CharSequence threeParagraphs = AppDetailsRecyclerViewAdapter.trimTrailingNewlines("Paragraph One\n\nParagraph Two\n\nParagraph Three\n\n");
        assertEquals("Paragraph One\n\nParagraph Two\n\nParagraph Three", threeParagraphs);

        CharSequence leadingAndExtraTrailing = AppDetailsRecyclerViewAdapter.trimTrailingNewlines("\n\n\nA\n\n\n");
        assertEquals("\n\n\nA", leadingAndExtraTrailing);
    }

    @Test
    public void commaSeparatedStrings() {
        assertNull(Utils.parseCommaSeparatedString(null));
        assertNull(Utils.parseCommaSeparatedString(""));

        String[] singleValue = Utils.parseCommaSeparatedString("single");
        assertNotNull(singleValue);
        assertEquals(1, singleValue.length);
        assertEquals("single", singleValue[0]);

        String[] tripleValue = Utils.parseCommaSeparatedString("One,TWO,three");
        assertNotNull(tripleValue);
        assertEquals(3, tripleValue.length);
        assertEquals("One", tripleValue[0]);
        assertEquals("TWO", tripleValue[1]);
        assertEquals("three", tripleValue[2]);

        assertNull(Utils.serializeCommaSeparatedString(null));
        assertNull(Utils.serializeCommaSeparatedString(new String[]{}));
        assertEquals("Single", Utils.serializeCommaSeparatedString(new String[]{"Single"}));
        assertEquals("One,TWO,three", Utils.serializeCommaSeparatedString(new String[]{"One", "TWO", "three"}));
    }

    @Test
    public void testFormatFingerprint() {
        Context context = ApplicationProvider.getApplicationContext();
        String badResult = Utils.formatFingerprint(context, "");
        // real fingerprints
        String formatted;
        formatted = Utils.formatFingerprint(context, fdroidFingerprint);
        assertFalse(formatted.equals(badResult));
        assertTrue(formatted.matches("[A-Z0-9][A-Z0-9] [A-Z0-9 ]+"));
        formatted = Utils.formatFingerprint(context, gpRepoFingerprint);
        assertFalse(formatted.equals(badResult));
        assertTrue(formatted.matches("[A-Z0-9][A-Z0-9] [A-Z0-9 ]+"));
        formatted = Utils.formatFingerprint(context, gpTest1Fingerprint);
        assertFalse(formatted.equals(badResult));
        assertTrue(formatted.matches("[A-Z0-9][A-Z0-9] [A-Z0-9 ]+"));
        // random garbage
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "234k2lk3jljwlk4j2lk3jlkmqwekljrlkj34lk2jlk2j34lkjl2k3j4lk2j34lja"));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "g000000000000000000000000000000000000000000000000000000000000000"));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "98273498723948728934789237489273p1928731982731982739182739817238"));
        // too short
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "C63AED1AC79D37C7B0474472AC6EFA6C3AB2B11A767A4F42CF360FA5496E3C5"));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "C63AED1"));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "f"));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, ""));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, null));
        // real digits but too long
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB"));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "C63AED1AC79D37C7B0474472AC6EFA6C3AB2B11A767A4F42CF360FA5496E3C50F"));
        assertEquals(
                badResult,
                Utils.formatFingerprint(context, "3082035e30820246a00302010202044c49cd00300d06092a864886f70d01010505003071310b300906035504061302554b3110300e06035504081307556e6b6e6f776e3111300f0603550407130857657468657262793110300e060355040a1307556e6b6e6f776e3110300e060355040b1307556e6b6e6f776e311930170603550403131043696172616e2047756c746e69656b73301e170d3130303732333137313032345a170d3337313230383137313032345a3071310b300906035504061302554b3110300e06035504081307556e6b6e6f776e3111300f0603550407130857657468657262793110300e060355040a1307556e6b6e6f776e3110300e060355040b1307556e6b6e6f776e311930170603550403131043696172616e2047756c746e69656b7330820122300d06092a864886f70d01010105000382010f003082010a028201010096d075e47c014e7822c89fd67f795d23203e2a8843f53ba4e6b1bf5f2fd0e225938267cfcae7fbf4fe596346afbaf4070fdb91f66fbcdf2348a3d92430502824f80517b156fab00809bdc8e631bfa9afd42d9045ab5fd6d28d9e140afc1300917b19b7c6c4df4a494cf1f7cb4a63c80d734265d735af9e4f09455f427aa65a53563f87b336ca2c19d244fcbba617ba0b19e56ed34afe0b253ab91e2fdb1271f1b9e3c3232027ed8862a112f0706e234cf236914b939bcf959821ecb2a6c18057e070de3428046d94b175e1d89bd795e535499a091f5bc65a79d539a8d43891ec504058acb28c08393b5718b57600a211e803f4a634e5c57f25b9b8c4422c6fd90203010001300d06092a864886f70d0101050500038201010008e4ef699e9807677ff56753da73efb2390d5ae2c17e4db691d5df7a7b60fc071ae509c5414be7d5da74df2811e83d3668c4a0b1abc84b9fa7d96b4cdf30bba68517ad2a93e233b042972ac0553a4801c9ebe07bf57ebe9a3b3d6d663965260e50f3b8f46db0531761e60340a2bddc3426098397fda54044a17e5244549f9869b460ca5e6e216b6f6a2db0580b480ca2afe6ec6b46eedacfa4aa45038809ece0c5978653d6c85f678e7f5a2156d1bedd8117751e64a4b0dcd140f3040b021821a8d93aed8d01ba36db6c82372211fed714d9a32607038cdfd565bd529ffc637212aaa2c224ef22b603eccefb5bf1e085c191d4b24fe742b17ab3f55d4e6f05ef"));
    }

    @Test
    public void testIsFileMatchingHash() {
        Utils.isFileMatchingHash(null, null, null);
        Utils.isFileMatchingHash(new File("/"), "", null);

        assertFalse(Utils.isFileMatchingHash(null, null, ""));
        assertFalse(Utils.isFileMatchingHash(null, null, SHA_256));
        assertFalse(Utils.isFileMatchingHash(new File("/"), null, SHA_256));
        assertFalse(Utils.isFileMatchingHash(new File("/"), "", SHA_256));

        assertTrue(Utils.isFileMatchingHash(TestUtils.copyResourceToTempFile("Norway_bouvet_europe_2.obf.zip"),
                "6e8a584e004c6cd26d3822a04b0591e355dc5d07b5a3d0f8e309443f47ad1208", SHA_256));
        assertTrue(Utils.isFileMatchingHash(TestUtils.copyResourceToTempFile("install_history_all"),
                "4ad118d4a600dcc104834635d248a89e337fc91b173163d646996b9c54d77372", SHA_256));
        assertFalse("wrong sha256 value",
                Utils.isFileMatchingHash(TestUtils.copyResourceToTempFile("simpleIndex.jar"),
                        "6e8a584e004c6cd26d3822a04b0591e355dc5d07b5a3d0f8e309443f47ad1208", SHA_256));

        File f = TestUtils.copyResourceToTempFile("additional_repos.xml");
        assertTrue(Utils.isFileMatchingHash(f,
                "47ad2284d3042373e6280012cc10e9b82f75352db6d6d9bab1e06934b7b1dab7", SHA_256));
        assertFalse("uppercase fails",
                Utils.isFileMatchingHash(f,
                        "47AD2284D3042373E6280012CC10E9B82F75352DB6D6D9BAB1E06934B7B1DAB7", SHA_256));
        assertFalse("one uppercase digit fails",
                Utils.isFileMatchingHash(f,
                        "47Ad2284d3042373e6280012cc10e9b82f75352db6d6d9bab1e06934b7b1dab7", SHA_256));
        assertFalse("missing digit fails",
                Utils.isFileMatchingHash(f,
                        "47ad2284d3042373e6280012cc10e9b82f75352db6d6d9bab1e06934b7b1dab", SHA_256));
        assertFalse("extra digit fails",
                Utils.isFileMatchingHash(f,
                        "47ad2284d3042373e6280012cc10e9b82f75352db6d6d9bab1e06934b7b1dab71", SHA_256));
        assertFalse("all zeros fails",
                Utils.isFileMatchingHash(f,
                        "0000000000000000000000000000000000000000000000000000000000000000", SHA_256));
        assertFalse("null fails",
                Utils.isFileMatchingHash(f, null, SHA_256));
        assertFalse("empty string fails",
                Utils.isFileMatchingHash(f, "", SHA_256));
    }

    @Test
    public void testCalcFingerprintString() {
        // these should pass
        assertEquals(fdroidFingerprint, Utils.calcFingerprint(fdroidPubkey));
        assertEquals(gpRepoFingerprint, Utils.calcFingerprint(gpRepoPubkey));
        assertEquals(gpTest0Fingerprint, Utils.calcFingerprint(gpTest0Pubkey));
        assertEquals(gpTest1Fingerprint, Utils.calcFingerprint(gpTest1Pubkey));

        // these should fail
        assertFalse(gpRepoFingerprint.equals(
                Utils.calcFingerprint(fdroidPubkey)));
        assertFalse(gpTest0Fingerprint.equals(
                Utils.calcFingerprint(fdroidPubkey)));
        assertFalse(gpTest1Fingerprint.equals(
                Utils.calcFingerprint(fdroidPubkey)));
        assertFalse(fdroidFingerprint.equals(
                Utils.calcFingerprint(gpRepoPubkey)));
        assertFalse(gpTest0Fingerprint.equals(
                Utils.calcFingerprint(gpRepoPubkey)));
        assertFalse(gpTest1Fingerprint.equals(
                Utils.calcFingerprint(gpRepoPubkey)));

        assertFalse(fingerprintShortByOneFingerprint.equals(
                Utils.calcFingerprint(fingerprintShortByOnePubkey)));
        assertFalse(fingerprintLongByOneFingerprint.equals(
                Utils.calcFingerprint(fingerprintLongByOnePubkey)));
        try {
            assertFalse(pubkeyShortByOneFingerprint.equals(
                    Utils.calcFingerprint(pubkeyShortByOnePubkey)));
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true); // we should get this Exception!
        }
        try {
            assertFalse(pubkeyLongByOneFingerprint.equals(
                    Utils.calcFingerprint(pubkeyLongByOnePubkey)));
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true); // we should get this Exception!
        }
    }

    @Test
    public void testGetFileHexDigest() throws IOException {
        File f = TestUtils.copyResourceToTempFile("largeRepo.xml");
        assertEquals("df1754aa4b56c86c06d7842dfd02064f0781c1f740f489d3fc158bb541c8d197",
                Utils.getFileHexDigest(f, "sha256"));
        f = TestUtils.copyResourceToTempFile("masterKeyIndex.jar");
        assertEquals("625d5aedcd0499fe04ebab81f3c7ae30c236cee653a914ffb587d890198f3aba",
                Utils.getFileHexDigest(f, "sha256"));
        f = TestUtils.copyResourceToTempFile("index.fdroid.2016-10-30.jar");
        assertEquals("c138b503c6475aa749585d0e3ad4dba3546b6d33ec485efd8ac8bd603d93fedb",
                Utils.getFileHexDigest(f, "sha-256"));
        f = TestUtils.copyResourceToTempFile("index.fdroid.2016-11-10.jar");
        assertEquals("93bea45814fd8955cabb957e7a3f8790d6c568eaa16fa30425c2d26c60490bde",
                Utils.getFileHexDigest(f, "SHA-256"));

        // zero size file should have a stable hex digest file
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                Utils.getFileHexDigest(File.createTempFile("asdf", "asdf"), SHA_256));

        assertNull(Utils.getFileHexDigest(new File("/kasdfkjasdhflkjasd"), SHA_256));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFileHexDigestBadAlgo() {
        File f = TestUtils.copyResourceToTempFile("additional_repos.xml");
        assertNull(Utils.getFileHexDigest(f, "FAKE"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetFileHexDigestNullFile() {
        assertNull(Utils.getFileHexDigest(null, SHA_256));
    }

    // TODO write tests that work with a Certificate

    @Test
    public void testIndexDatesWithTimeZones() {
        for (int h = 0; h < 12; h++) {
            for (int m = 0; m < 60; m = m + 15) {
                TimeZone.setDefault(TimeZone.getTimeZone(String.format("GMT+%d%02d", h, m)));

                String timeString = "2017-11-27_20:13:24";
                Date time = Utils.parseTime(timeString, null);
                assertEquals("The String representation must match", timeString, Utils.formatTime(time, null));
                assertEquals(timeString + " failed to parse", 1511813604000L, time.getTime());
                assertEquals("time zones should match", -((h * 60) + m), time.getTimezoneOffset());

                TimeZone.setDefault(TimeZone.getTimeZone(String.format("GMT+%d%02d", h, m)));
                String dateString = "2017-11-27";
                Date date = Utils.parseDate(dateString, null);
                assertEquals("The String representation must match", dateString, Utils.formatDate(date, null));
                assertEquals(dateString + " failed to parse", 1511740800000L, date.getTime());
                assertEquals("time zones should match", -((h * 60) + m), date.getTimezoneOffset());
            }
        }
    }

    /**
     * Test the replacement for the ancient fingerprint algorithm.
     *
     * @see org.fdroid.fdroid.data.Apk#sig
     */
    @Test
    public void testGetsig() {
        /*
         * I don't fully understand the loop used here. I've copied it verbatim
         * from getsig.java bundled with FDroidServer. I *believe* it is taking
         * the raw byte encoding of the certificate & converting it to a byte
         * array of the hex representation of the original certificate byte
         * array. This is then MD5 sum'd. It's a really bad way to be doing this
         * if I'm right... If I'm not right, I really don't know! see lines
         * 67->75 in getsig.java bundled with Fdroidserver
         */
        for (int length : new int[]{256, 345, 1233, 4032, 12092}) {
            byte[] rawCertBytes = new byte[length];
            new Random().nextBytes(rawCertBytes);
            final byte[] fdroidSig = new byte[rawCertBytes.length * 2];
            for (int j = 0; j < rawCertBytes.length; j++) {
                byte v = rawCertBytes[j];
                int d = (v >> 4) & 0xF;
                fdroidSig[j * 2] = (byte) (d >= 10 ? ('a' + d - 10) : ('0' + d));
                d = v & 0xF;
                fdroidSig[j * 2 + 1] = (byte) (d >= 10 ? ('a' + d - 10) : ('0' + d));
            }
            String sig = DigestUtils.md5Hex(fdroidSig);
            assertEquals(sig, Utils.getsig(rawCertBytes));

            PackageInfo packageInfo = new PackageInfo();
            packageInfo.signatures = new Signature[]{new Signature(rawCertBytes)};
            assertEquals(sig, Utils.getPackageSig(packageInfo));
        }
    }

    @Test
    public void testGetAntifeatureSQLFilterWithNone() {
        Context context = ApplicationProvider.getApplicationContext();
        Preferences.setupForTests(context);
        assertEquals("fdroid_app.antiFeatures IS NULL", Utils.getAntifeatureSQLFilter(context));
    }

    @Test
    public void testGetAntifeatureSQLFilter() {
        CursorLoader cursorLoader = new CursorLoader(
                context,
                AppProvider.getLatestTabUri(),
                Schema.AppMetadataTable.Cols.ALL,
                Utils.getAntifeatureSQLFilter(context),
                null,
                null);
        cursor = cursorLoader.loadInBackground();
        assertNotNull(cursor);
    }

    @Test(expected = SAXParseException.class)
    public void testXMLReaderXXEFileAccess() throws ParserConfigurationException, SAXException, IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "  <!DOCTYPE foo [  \n" +
                "    <!ELEMENT foo ANY >\n" +
                "    <!ENTITY xxe SYSTEM \"file:///etc/hosts\" >]><foo>&xxe;</foo>";
        final XMLReader reader = Utils.newXMLReaderInstance();
        reader.parse(new InputSource(new StringReader(xml)));
    }

    @Test(expected = SAXParseException.class)
    public void testXMLReaderXXEUrlAccess() throws ParserConfigurationException, SAXException, IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "  <!DOCTYPE foo [  \n" +
                "    <!ELEMENT foo ANY >\n" +
                "    <!ENTITY xxe SYSTEM \"https://example.com\" >]><foo>&xxe;</foo>";
        final XMLReader reader = Utils.newXMLReaderInstance();
        reader.parse(new InputSource(new StringReader(xml)));
    }
}
