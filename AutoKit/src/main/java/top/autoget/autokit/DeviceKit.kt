package top.autoget.autokit

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_PHONE_STATE
import android.content.res.Configuration
import android.content.res.Resources
import android.provider.Settings
import android.telephony.CellLocation
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import top.autoget.autokit.AKit.app
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.VersionKit.aboveLollipop
import top.autoget.autokit.VersionKit.aboveOreo
import java.io.IOException
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.util.*

object DeviceKit : LoggerKit {
    val isAdbEnabled: Boolean
        get() = Settings.Secure.getInt(app.contentResolver, Settings.Global.ADB_ENABLED, 0) > 0
    val numberCpuSerial: String
        get() = try {
            Runtime.getRuntime().exec("cat/proc/cpuinfo").inputStream.use { inputStream ->
                InputStreamReader(inputStream).use { inputStreamReader ->
                    LineNumberReader(inputStreamReader).use { lineNumberReader ->
                        for (i in 1..99) {
                            lineNumberReader.readLine()?.run {
                                if (indexOf("Serial") > -1)
                                    return substring(indexOf(":") + 1, length).trim { it <= ' ' }
                            } ?: break
                        }
                        "0000000000000000"
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            "0000000000000000"
        }//手机CPU序列号
    val statePhone: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.run {
            """
                |SubscriberId(IMSI) = $subscriberId
                |DeviceId(IMEI) = $deviceId
                |DeviceSoftwareVersion = $deviceSoftwareVersion
                |SimState = $simState
                |SimCountryIso = $simCountryIso
                |SimOperator = $simOperator
                |SimOperatorName = $simOperatorName
                |PhoneType = $phoneType
                |SimSerialNumber = $simSerialNumber
                |NetworkCountryIso = $networkCountryIso
                |NetworkOperator = $networkOperator
                |NetworkOperatorName = $networkOperatorName
                |NetworkType = $networkType
                |Line1Number = $line1Number
                |VoiceMailNumber = $voiceMailNumber
                """.trimMargin()
        }
    val imsi: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.subscriberId//唯一用户ID
    val carrierDevice: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = imsi.run {
            when {
                isEmpty() -> ""
                else -> when {
                    startsWith("46000") || startsWith("46002") || startsWith("46007") ||
                            startsWith("46020") -> "China Mobile"
                    startsWith("46001") || startsWith("46006") || startsWith("46009") -> "China Unicom"
                    startsWith("46003") || startsWith("46005") || startsWith("46011") -> "China Telecom"
                    else -> ""
                }
            }
        }//运营商
    val androidId: String
        get() = Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    val deviceId: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.run {
            when {
                aboveOreo -> when {
                    isNotSpace(imei) -> imei
                    isNotSpace(meid) -> meid
                    else -> ""
                }
                else -> when {
                    isNotSpace(deviceId) -> deviceId//唯一设备ID：GSM->IMEI；CDMA->MEID。
                    isNotSpace(androidId) -> androidId
                    else -> ""
                }
            }
        }
    val imei: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.run {
            when {
                aboveOreo -> imei
                aboveLollipop -> try {
                    javaClass.getDeclaredMethod("getImei")
                        .apply { isAccessible = true }.invoke(this) as String
                } catch (e: Exception) {
                    "".apply { error("$loggerTag->getImei: $e") }
                }
                else -> deviceId?.run { if (length == 15) this else androidId } ?: androidId
            }
        }

    @RequiresPermission(READ_PHONE_STATE)
    fun getImei(slotId: Int): String = when {
        aboveOreo -> app.telephonyManager.getImei(slotId)
        aboveLollipop -> try {
            app.telephonyManager.javaClass
                .getDeclaredMethod("getImei", Int::class.javaPrimitiveType)
                .apply { isAccessible = true }.invoke(app.telephonyManager, slotId) as String
        } catch (e: Exception) {
            imei.apply { error("$loggerTag->getImei: $e") }
        }
        else -> imei
    }

    val meid: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.run { if (aboveOreo) meid else deviceId }

    @RequiresPermission(READ_PHONE_STATE)
    fun getMeid(slotId: Int): String = when {
        aboveOreo -> app.telephonyManager.getMeid(slotId)
        else -> meid
    }

    val deviceSoftVersion: String?
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.deviceSoftwareVersion//设备软件版本号
    val stateSim: Int
        get() = app.telephonyManager.simState
    val isSimReady: Boolean
        get() = stateSim == TelephonyManager.SIM_STATE_READY
    val simCountryIso: String
        get() = if (isSimReady) app.telephonyManager.simCountryIso else ""
    val simCountry: String
        get() = when {
            isSimReady -> simCountryIso.toUpperCase(Locale.getDefault())
            else -> Locale.getDefault().run { country.toUpperCase(this) }
        }
    private val countryCodeMap: MutableMap<String, String>
        get() = mutableMapOf(
            Pair("AL", "+355"),
            Pair("DZ", "+213"),
            Pair("AF", "+93"),
            Pair("AR", "+54"),
            Pair("AE", "+971"),
            Pair("AW", "+297"),
            Pair("OM", "+968"),
            Pair("AZ", "+994"),
            Pair("AC", "+247"),
            Pair("EG", "+20"),
            Pair("ET", "+251"),
            Pair("IE", "+353"),
            Pair("EE", "+372"),
            Pair("AD", "+376"),
            Pair("AO", "+244"),
            Pair("AI", "+1"),
            Pair("AG", "+1"),
            Pair("AT", "+43"),
            Pair("AX", "+358"),
            Pair("AU", "+61"),
            Pair("BB", "+1"),
            Pair("PG", "+675"),
            Pair("BS", "+1"),
            Pair("PK", "+92"),
            Pair("PY", "+595"),
            Pair("PS", "+970"),
            Pair("BH", "+973"),
            Pair("PA", "+507"),
            Pair("BR", "+55"),
            Pair("BY", "+375"),
            Pair("BM", "+1"),
            Pair("BG", "+359"),
            Pair("MP", "+1"),
            Pair("BJ", "+229"),
            Pair("BE", "+32"),
            Pair("IS", "+354"),
            Pair("PR", "+1"),
            Pair("PL", "+48"),
            Pair("BA", "+387"),
            Pair("BO", "+591"),
            Pair("BZ", "+501"),
            Pair("BW", "+267"),
            Pair("BT", "+975"),
            Pair("BF", "+226"),
            Pair("BI", "+257"),
            Pair("KP", "+850"),
            Pair("GQ", "+240"),
            Pair("DK", "+45"),
            Pair("DE", "+49"),
            Pair("TL", "+670"),
            Pair("TG", "+228"),
            Pair("DO", "+1"),
            Pair("DM", "+1"),
            Pair("RU", "+7"),
            Pair("EC", "+593"),
            Pair("ER", "+291"),
            Pair("FR", "+33"),
            Pair("FO", "+298"),
            Pair("PF", "+689"),
            Pair("GF", "+594"),
            Pair("VA", "+39"),
            Pair("PH", "+63"),
            Pair("FJ", "+679"),
            Pair("FI", "+358"),
            Pair("CV", "+238"),
            Pair("FK", "+500"),
            Pair("GM", "+220"),
            Pair("CG", "+242"),
            Pair("CD", "+243"),
            Pair("CO", "+57"),
            Pair("CR", "+506"),
            Pair("GG", "+44"),
            Pair("GD", "+1"),
            Pair("GL", "+299"),
            Pair("GE", "+995"),
            Pair("CU", "+53"),
            Pair("GP", "+590"),
            Pair("GU", "+1"),
            Pair("GY", "+592"),
            Pair("KZ", "+7"),
            Pair("HT", "+509"),
            Pair("KR", "+82"),
            Pair("NL", "+31"),
            Pair("BQ", "+599"),
            Pair("SX", "+1"),
            Pair("ME", "+382"),
            Pair("HN", "+504"),
            Pair("KI", "+686"),
            Pair("DJ", "+253"),
            Pair("KG", "+996"),
            Pair("GN", "+224"),
            Pair("GW", "+245"),
            Pair("CA", "+1"),
            Pair("GH", "+233"),
            Pair("GA", "+241"),
            Pair("KH", "+855"),
            Pair("CZ", "+420"),
            Pair("ZW", "+263"),
            Pair("CM", "+237"),
            Pair("QA", "+974"),
            Pair("KY", "+1"),
            Pair("CC", "+61"),
            Pair("KM", "+269"),
            Pair("XK", "+383"),
            Pair("CI", "+225"),
            Pair("KW", "+965"),
            Pair("HR", "+385"),
            Pair("KE", "+254"),
            Pair("CK", "+682"),
            Pair("CW", "+599"),
            Pair("LV", "+371"),
            Pair("LS", "+266"),
            Pair("LA", "+856"),
            Pair("LB", "+961"),
            Pair("LT", "+370"),
            Pair("LR", "+231"),
            Pair("LY", "+218"),
            Pair("LI", "+423"),
            Pair("RE", "+262"),
            Pair("LU", "+352"),
            Pair("RW", "+250"),
            Pair("RO", "+40"),
            Pair("MG", "+261"),
            Pair("IM", "+44"),
            Pair("MV", "+960"),
            Pair("MT", "+356"),
            Pair("MW", "+265"),
            Pair("MY", "+60"),
            Pair("ML", "+223"),
            Pair("MK", "+389"),
            Pair("MH", "+692"),
            Pair("MQ", "+596"),
            Pair("YT", "+262"),
            Pair("MU", "+230"),
            Pair("MR", "+222"),
            Pair("US", "+1"),
            Pair("AS", "+1"),
            Pair("VI", "+1"),
            Pair("MN", "+976"),
            Pair("MS", "+1"),
            Pair("BD", "+880"),
            Pair("PE", "+51"),
            Pair("FM", "+691"),
            Pair("MM", "+95"),
            Pair("MD", "+373"),
            Pair("MA", "+212"),
            Pair("MC", "+377"),
            Pair("MZ", "+258"),
            Pair("MX", "+52"),
            Pair("NA", "+264"),
            Pair("ZA", "+27"),
            Pair("SS", "+211"),
            Pair("NR", "+674"),
            Pair("NI", "+505"),
            Pair("NP", "+977"),
            Pair("NE", "+227"),
            Pair("NG", "+234"),
            Pair("NU", "+683"),
            Pair("NO", "+47"),
            Pair("NF", "+672"),
            Pair("PW", "+680"),
            Pair("PT", "+351"),
            Pair("JP", "+81"),
            Pair("SE", "+46"),
            Pair("CH", "+41"),
            Pair("SV", "+503"),
            Pair("WS", "+685"),
            Pair("RS", "+381"),
            Pair("SL", "+232"),
            Pair("SN", "+221"),
            Pair("CY", "+357"),
            Pair("SC", "+248"),
            Pair("SA", "+966"),
            Pair("BL", "+590"),
            Pair("CX", "+61"),
            Pair("ST", "+239"),
            Pair("SH", "+290"),
            Pair("PN", "+870"),
            Pair("KN", "+1"),
            Pair("LC", "+1"),
            Pair("MF", "+590"),
            Pair("SM", "+378"),
            Pair("PM", "+508"),
            Pair("VC", "+1"),
            Pair("LK", "+94"),
            Pair("SK", "+421"),
            Pair("SI", "+386"),
            Pair("SJ", "+47"),
            Pair("SZ", "+268"),
            Pair("SD", "+249"),
            Pair("SR", "+597"),
            Pair("SB", "+677"),
            Pair("SO", "+252"),
            Pair("TJ", "+992"),
            Pair("TH", "+66"),
            Pair("TZ", "+255"),
            Pair("TO", "+676"),
            Pair("TC", "+1"),
            Pair("TA", "+290"),
            Pair("TT", "+1"),
            Pair("TN", "+216"),
            Pair("TV", "+688"),
            Pair("TR", "+90"),
            Pair("TM", "+993"),
            Pair("TK", "+690"),
            Pair("WF", "+681"),
            Pair("VU", "+678"),
            Pair("GT", "+502"),
            Pair("VE", "+58"),
            Pair("BN", "+673"),
            Pair("UG", "+256"),
            Pair("UA", "+380"),
            Pair("UY", "+598"),
            Pair("UZ", "+998"),
            Pair("GR", "+30"),
            Pair("ES", "+34"),
            Pair("EH", "+212"),
            Pair("SG", "+65"),
            Pair("NC", "+687"),
            Pair("NZ", "+64"),
            Pair("HU", "+36"),
            Pair("SY", "+963"),
            Pair("JM", "+1"),
            Pair("AM", "+374"),
            Pair("YE", "+967"),
            Pair("IQ", "+964"),
            Pair("UM", "+1"),
            Pair("IR", "+98"),
            Pair("IL", "+972"),
            Pair("IT", "+39"),
            Pair("IN", "+91"),
            Pair("ID", "+62"),
            Pair("GB", "+44"),
            Pair("VG", "+1"),
            Pair("IO", "+246"),
            Pair("JO", "+962"),
            Pair("VN", "+84"),
            Pair("ZM", "+260"),
            Pair("JE", "+44"),
            Pair("TD", "+235"),
            Pair("GI", "+350"),
            Pair("CL", "+56"),
            Pair("CF", "+236"),
            Pair("CN", "+86"),
            Pair("MO", "+853"),
            Pair("TW", "+886"),
            Pair("HK", "+852")
        )

    @JvmOverloads
    fun getCountryCode(defaultValue: String = "+86"): String =
        countryCodeMap[simCountry] ?: defaultValue

    val simOperator: String
        get() = if (isSimReady) app.telephonyManager.simOperator else ""//移动国家码网络码
    val simCarrier: String
        get() = when (simOperator) {
            "46000", "46002", "46007", "46020" -> "中国移动"
            "46001", "46006", "46009" -> "中国联通"
            "46003", "46005", "46011" -> "中国电信"
            else -> simOperator
        }
    val simOperatorName: String
        get() = if (isSimReady) app.telephonyManager.simOperatorName else ""//服务商名称
    val simType: Int
        get() = app.telephonyManager.phoneType//PHONE_TYPE_NONE、PHONE_TYPE_GSM移动联通、PHONE_TYPE_CDMA电信、PHONE_TYPE_SIP。
    val isPhone: Boolean
        get() = simType != TelephonyManager.PHONE_TYPE_NONE
    val isTablet: Boolean
        get() = Resources.getSystem().configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    val numberSimSerial: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.simSerialNumber
    val networkCountryIso: String
        get() = app.telephonyManager.networkCountryIso
    val networkOperator: String
        get() = app.telephonyManager.networkOperator
    val networkOperatorName: String
        get() = app.telephonyManager.networkOperatorName.toLowerCase(Locale.getDefault())//运营商
    val networkType: Int
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.networkType
    val numberPhone: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.line1Number//手机号码
    val numberVoiceMail: String?
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.telephonyManager.voiceMailNumber
    val stateCall: Int
        get() = app.telephonyManager.callState//手机状态：0无活动；1响铃；2待机。
    val stateLocation: CellLocation
        @RequiresPermission(ACCESS_FINE_LOCATION)
        get() = app.telephonyManager.cellLocation//手机方位
}