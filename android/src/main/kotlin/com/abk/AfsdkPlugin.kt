package com.abk

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.abk.afsdk.Camera2Activity
import com.abk.afsdk.R
import com.abk.afsdk.camera2.Camera2Helper
import com.abk.utils.HexUtil
import com.abk.utils.LogUtil
import com.abk.utils.PrintUtil
import com.huawei.hms.ml.scan.HmsScan
import com.szanfu.sdk.api.ISdkServiceManager
import com.szanfu.sdk.api.card.ICardBinderService
import com.szanfu.sdk.api.card.ICardTestCallback
import com.szanfu.sdk.api.constant.ServiceID
import com.szanfu.sdk.api.emv.DalcscIsoMifareCmdResult
import com.szanfu.sdk.api.emv.IEmvBinderService
import com.szanfu.sdk.api.error.ErrorCode
import com.szanfu.sdk.api.printer.IPrinterBinderService
import com.szanfu.sdk.api.printer.IPrinterCallback
import com.szanfu.sdk.api.system.ISystemBinderService
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import org.json.JSONObject
import java.util.Arrays

/** AfsdkPlugin */
class AfsdkPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var mSdkServiceManager: ISdkServiceManager? = null
    var isServiceBound: Boolean = false
    private var activity: Activity? = null
    private var resultToReturn: MethodChannel.Result? = null

    private val REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW: Int = 0X115
    private val FIND_CARD_TIMEOUT: Int = 30
    private var dalcscIsoMifareCmdResult: DalcscIsoMifareCmdResult? = null
    private var cardJson = JSONObject()


    private var mPrinterBinderService: IPrinterBinderService? = null
    private var mSystemBinderService: ISystemBinderService? = null
    private var mCardBinderService: ICardBinderService? = null
    private var mIEmvBinderService: IEmvBinderService? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "afsdk")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        resultToReturn = result
        if (call.method == "printImage") {
            printImage(call.argument<ByteArray>("image")!!)
        } else if (call.method == "beepBuzzer") {
            beepTest()
        } else if (call.method == "readCard") {
            mifareCardData()
        } else if (call.method == "scanCode") {
            val intent = Intent(activity!!, Camera2Activity::class.java)
            intent.putExtra(Camera2Activity.CAMERA_ID, Camera2Helper.CAMERA_ID_BACK)
            intent.putExtra(Camera2Activity.DECODE_LIB, Camera2Helper.DECODE_LIB_HW)
            activity!!.startActivityForResult(
                intent,
                REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW
            )
        } else {
            result.notImplemented()
        }
    }

    private fun printImage(bytes: ByteArray) {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                try {
                    if (mPrinterBinderService == null) { //在非主线程当中可以这样更新UI
                        resultToReturn?.error("101", "print failed due to service null", null)
                        resultToReturn = null
                        activity?.runOnUiThread(object : Runnable {
                            override fun run() {
                                Toast.makeText(
                                    activity!!,
                                    activity!!.resources!!.getString(R.string.print_serviceerror),
                                    Toast.LENGTH_SHORT
                                )
                            }
                        })
                        return
                    }
//                    if (com.szanfu.sdk.test.activity.PrinterServiceActivity.imgBmp != null) {
//                        val textBmp: Bitmap = creatTextBitmap(printMsg(), 384, 950)
//                        //两张bitmap拼接成一张bitmap
//                        printBmp = BitmapUtil.addBitmap(
//                            textBmp,
//                            com.szanfu.sdk.test.activity.PrinterServiceActivity.imgBmp
//                        )
//                    } else {
                    val printBmp: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                    }

                    LogUtil.d("bmp width: " + printBmp.getWidth() + "." + "\n" + "bmp height: " + printBmp.getHeight() + "." + "\n")
                    val x = 0
                    val width = printBmp.getWidth()
                    val height = printBmp.getHeight() + 30
                    val bitW: Int = PrintUtil.getPaddingBitWidth(width)
                    val bitmapData: ByteArray? = PrintUtil.getBitmapData(printBmp)
                    Log.e("szaf", "printerTest 发送打印请求接口: ")
                    mPrinterBinderService!!.printBitmap(
                        x,
                        bitmapData,
                        bitW,
                        height,
                        object : IPrinterCallback.Stub() {
                            @Throws(RemoteException::class)
                            override fun onResult(
                                errorCode: Int,
                                errorMsg: String?,
                                subErrorCode: Int
                            ) {
                                if (errorCode == ErrorCode.SUCCESS) {
                                    LogUtil.d("Print success")
                                    resultToReturn?.error("102", "print success", null)
                                    resultToReturn = null
                                } else {
                                    Toast.makeText(
                                        activity!!,
                                        "Error Code: " + errorCode + "\n" +
                                                "Sub Error Code: " + subErrorCode + "\n" +
                                                "Error Msg: " + errorMsg + "\n", Toast.LENGTH_SHORT
                                    ).show()
                                    resultToReturn?.error(
                                        "103",
                                        "print error with error :: $errorMsg $errorCode",
                                        null
                                    )
                                    resultToReturn = null

                                }
                            }
                        })
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    LogUtil.e("Print error", e)
                    resultToReturn?.error("104", "print error $e", null)
                    resultToReturn = null
                }
            }
        })
    }


    /**
     * buzz the device
     */
    private fun beepTest() {
        try {
            if (mSystemBinderService == null) {
                resultToReturn?.error("100", "service connection issue", null)
                resultToReturn = null
                return
            }

            val ret: Int = mSystemBinderService!!.setBeepStatus(true, 9600, 9600, 1000)
            if (ret == 0) {
                resultToReturn?.success(ret)
                resultToReturn = null
            } else {
                resultToReturn?.error(ret.toString(), "error while buzz", null)
                resultToReturn = null
            }

            LogUtil.d("-----------------Buzz Test :: $ret---------------------------")
        } catch (e: RemoteException) {
            e.printStackTrace()
            resultToReturn?.error("101", e.message, null)
            resultToReturn = null
        }
    }

    private fun readRFCard() {
        if (mCardBinderService == null) {
            resultToReturn?.error("100", "service connection issue", null)
            resultToReturn = null
            return
        }
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                try {
                    val cardType: Int = mCardBinderService!!.openFindCardForEMV(
                        4,
                        FIND_CARD_TIMEOUT
                    )
                    LogUtil.d("openFindCardForEMV : " + cardType)
                    mCardBinderService!!.closeFindCardForEMV() // 寻卡完毕之后关闭寻卡
                    resultToReturn?.success(cardType)
                    resultToReturn = null
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    resultToReturn?.error("101", e.message, null)
                    resultToReturn = null
                }
            }
        })
    }

    private fun mifareCardData() {
        try {
            cardJson = JSONObject()
            mCardBinderService!!.testRFCard(
                FIND_CARD_TIMEOUT,
                ICardTestCallbackProxy()
            )
        } catch (e: RemoteException) {
            e.printStackTrace()
            resultToReturn?.error("101", e.message, null)
            resultToReturn = null
        }
    }

    inner class ICardTestCallbackProxy internal constructor() :
        ICardTestCallback.Stub() {
        @Throws(RemoteException::class)
        override fun onFindCardStart() {
//            appendText(getResources().getString(R.string.start_looking_for_a_card))
        }

        @Throws(RemoteException::class)
        override fun onFindCardSuccess(cardType: Int) {
            if (mIEmvBinderService == null) {
                resultToReturn?.error("100", "service connection issue", null)
                resultToReturn = null
                return
            }
//            appendText(getResources().getString(R.string.the_card_type_is_searched_successfully) + cardType)
            val ret: Int = mIEmvBinderService!!.apiDALCscIsMifare()
//            appendText("apiDALCscIsMifare ret : " + ret)

            cardJson.put("cardType", cardType)
            cardJson.put("apiDALCscIsMifare_ret", ret)

            /**
             * MifareOperaData opcode
             * MIF_OPTFHIR=4,//M1 card: read
             * MIF_OPT-WRITE=5,//M1 card: write
             * MIF_OPT-INCREMENT=6,//M1 card:++
             * MIF_OPT_deCREMENT=7,//M1 card: - minus
             * MIF_OPT_Compatible_SRITE=8,//M0 card: compatible with writin
             */
            val operateCode: Int
            // After performing read, add, and subtract operations, read the data of the corresponding card block (Read Blocknum data/Upda
            val operateData: ByteArray
            //length of data read (The length of writing or reading Blocknum data)
            val operateDataLen: IntArray?
            LogUtil.e(
                "onFindCardSuccess: Read data.... $ret :: type --> $cardType"
            )
            if (1 == ret && 4 == cardType) {

                operateCode = 4
                operateData = ByteArray(512)
                operateDataLen = IntArray(1)

                try {
                    // The key required for M1 card authentication
                    val cardKey = ByteArray(6)
                    Arrays.fill(cardKey, 0xFF.toByte())
                    //m1 card authentication type, KEYA: 0, KEYB: 1
                    val keyType = 0
                    //The block number for operating M1 and M0 cards
                    val blockNum = 5
                    dalcscIsoMifareCmdResult = mIEmvBinderService!!.apiDALcscIsoMifareCmd(
                        operateCode,
                        cardKey,
                        keyType,
                        blockNum,
                        operateData,
                        operateDataLen
                    )
                    val m1_opt_dataOut = ByteArray(operateDataLen[0])
                    System.arraycopy(operateData, 0, m1_opt_dataOut, 0, operateDataLen[0])

                    try {
                        cardJson = JSONObject().apply {
                            put("blockNum", blockNum)
                            put(
                                "dataLen",
                                (if (dalcscIsoMifareCmdResult == null) "null" else dalcscIsoMifareCmdResult!!.getM1_opt_data_len())
                            )
                            put(
                                "data",
                                (if (dalcscIsoMifareCmdResult == null) "null" else HexUtil.bcd2str(
                                    m1_opt_dataOut
                                ))
                            )
                        }
                        val map = jsonObjectToMap(cardJson)
                        resultToReturn?.success(map)
                        resultToReturn = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        resultToReturn?.error("101", e.message, null)
                        resultToReturn = null
                    }

//                    appendText("blockNum :" + blockNum)
//                    appendText("Received data Len : " + (if (dalcscIsoMifareCmdResult == null) "null" else dalcscIsoMifareCmdResult.getM1_opt_data_len()))
//                    appendText(
//                        "Received data : " + (if (dalcscIsoMifareCmdResult == null) "null" else HexUtil.bcd2str(
//                            m1_opt_dataOut
//                        )) + "\n"
//                    )

                    //                    operateDataLen[0] = 0;
//                    for (int i = 0; i < 16; i++) {
//                        blockNum = i;
//                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        @Throws(RemoteException::class)
        override fun onTrackDataRead(
            track1Data: ByteArray?,
            track2Data: ByteArray?,
            track3Data: ByteArray?
        ) {
//            appendText("Track1 Data: " + (if (track1Data == null) "null" else (String(track1Data)).trim { it <= ' ' }))
//            appendText("Track2 Data: " + (if (track2Data == null) "null" else (String(track2Data)).trim { it <= ' ' }))
//            appendText("Track3 Data: " + (if (track3Data == null) "null" else (String(track3Data)).trim { it <= ' ' }))
//            if (track1Data != null) {
//                Log.e("szaf", "onTrackDataRead:收到磁条卡返回数据结果")
//            }
        }

        @Throws(RemoteException::class)
        override fun onCardDataRead(cardData: ByteArray?) {
            val cardDataString = (if (cardData == null) "" else HexUtil.bcd2str(cardData))
            LogUtil.d("Card Data: $cardDataString")
            cardJson.put("cardData", cardDataString)
        }

        @Throws(RemoteException::class)
        override fun onFindSamCardStart() {
            LogUtil.d("寻SAM卡====》》》")
        }

        @Throws(RemoteException::class)
        override fun onResult(errorCode: Int, errorMsg: String?, subErrorCode: Int) {
            if (errorCode == ErrorCode.SUCCESS) {
                LogUtil.d("Success card read")
                val map = jsonObjectToMap(cardJson)
                resultToReturn?.success(map)
                resultToReturn = null
            } else {
                LogUtil.e(
                    ("Error Code: " + errorCode + "  Sub Error Code: " + subErrorCode
                            + "\nError Msg: " + errorMsg + "\n")
                )
                resultToReturn?.error(errorCode.toString(), errorMsg, null)
                resultToReturn = null
            }
            mCardBinderService?.closeFindCardForEMV()
            LogUtil.d("**************************************")
        }
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        bindService()
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        bindService()

    }

    override fun onDetachedFromActivity() {
        unbindService()
    }

    private fun bindService() {
        LogUtil.d("bindService: called")
        val intent = Intent()
        intent.setPackage("com.szanfu.sdk.service")
        intent.setAction("com.szanfu.sdk.service.SDK_SERVICE")

        activity?.bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        LogUtil.d("ListButtonBaseActivity", "--------- 注销服务 Unbind from Service ------------")
        if (isServiceBound) {
            activity?.unbindService(conn)
        }
    }

    var conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
            mSdkServiceManager = null
            LogUtil.d("SDK binder service disconnected.")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            LogUtil.d("SDK binder service connected.")

            isServiceBound = true
            mSdkServiceManager = ISdkServiceManager.Stub.asInterface(service)
            if (mSdkServiceManager != null) {
                try {

                    mSdkServiceManager!!.asBinder().linkToDeath(object : IBinder.DeathRecipient {
                        override fun binderDied() {
                            LogUtil.d("SDK binder service binderDied.")
                            bindService()
                        }
                    }, 0)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

                onServiceConnected(mSdkServiceManager!!)
            }
        }
    }

    fun onServiceConnected(sdkServiceManager: ISdkServiceManager) {
        try {
            mSystemBinderService =
                ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM))
            mPrinterBinderService =
                IPrinterBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_PRINTER))
            mCardBinderService =
                ICardBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_CARD))
            mIEmvBinderService =
                IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        if (isServiceBound) {
            unbindService()
        }
        channel.setMethodCallHandler(null)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        if (resultCode != Activity.RESULT_OK || data == null) {
            resultToReturn?.error(requestCode.toString(), "Error while getting result", null)
            resultToReturn = null
            return false
        }

        var hmsScan: HmsScan? = null
        if (requestCode == REQUEST_CODE_CUSTOM_BACK_CAMERA_BY_HW) {
            hmsScan = data.getParcelableExtra<HmsScan?>(Camera2Activity.SCAN_RESULT)

            val time = data.getLongExtra(Camera2Activity.SCAN_TIME, -1)

            if (hmsScan != null) {
                val originalValue = hmsScan.getOriginalValue()
                LogUtil.i("szaf", "hmsScan : " + originalValue)
//                rawResult.setText(hmsScan.getOriginalValue())
                var codeFormat = ""

                if (hmsScan.getScanType() == HmsScan.QRCODE_SCAN_TYPE) {
                    codeFormat = "QR code"
                } else if (hmsScan.getScanType() == HmsScan.AZTEC_SCAN_TYPE) {
                    codeFormat = "AZTEC code"
                } else if (hmsScan.getScanType() == HmsScan.DATAMATRIX_SCAN_TYPE) {
                    codeFormat = "DATAMATRIX code"
                } else if (hmsScan.getScanType() == HmsScan.PDF417_SCAN_TYPE) {
                    codeFormat = "PDF417 code"
                } else if (hmsScan.getScanType() == HmsScan.CODE93_SCAN_TYPE) {
                    codeFormat = "CODE93"
                } else if (hmsScan.getScanType() == HmsScan.CODE39_SCAN_TYPE) {
                    codeFormat = "CODE39"
                } else if (hmsScan.getScanType() == HmsScan.CODE128_SCAN_TYPE) {
                    codeFormat = "CODE128"
                } else if (hmsScan.getScanType() == HmsScan.EAN13_SCAN_TYPE) {
                    codeFormat = "EAN13 code"
                } else if (hmsScan.getScanType() == HmsScan.EAN8_SCAN_TYPE) {
                    codeFormat = "EAN8 code"
                } else if (hmsScan.getScanType() == HmsScan.ITF14_SCAN_TYPE) {
                    codeFormat = "ITF14 code"
                } else if (hmsScan.getScanType() == HmsScan.UPCCODE_A_SCAN_TYPE) {
                    codeFormat = "UPCCODE_A"
                } else if (hmsScan.getScanType() == HmsScan.UPCCODE_E_SCAN_TYPE) {
                    codeFormat = "UPCCODE_E"
                } else if (hmsScan.getScanType() == HmsScan.CODABAR_SCAN_TYPE) {
                    codeFormat = "CODABAR"
                }


                //Show the barcode result.
                var scanType = ""
                if (hmsScan.getScanType() == HmsScan.QRCODE_SCAN_TYPE) {

                    if (hmsScan.getScanTypeForm() == HmsScan.PURE_TEXT_FORM) {
                        scanType = "Text"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.EVENT_INFO_FORM) {
                        scanType = "Event"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.CONTACT_DETAIL_FORM) {
                        scanType = "Contact"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.DRIVER_INFO_FORM) {
                        scanType = "License"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.EMAIL_CONTENT_FORM) {
                        scanType = "Email"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.LOCATION_COORDINATE_FORM) {
                        scanType = "Location"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.TEL_PHONE_NUMBER_FORM) {
                        scanType = "Tel"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.SMS_FORM) {
                        scanType = "SMS"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.WIFI_CONNECT_INFO_FORM) {
                        scanType = "Wi-Fi"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.URL_FORM) {
                        scanType = "WebSite"
                    } else {
                        scanType = "Text"
                    }
                } else if (hmsScan.getScanType() == HmsScan.EAN13_SCAN_TYPE) {
                    if (hmsScan.getScanTypeForm() == HmsScan.ISBN_NUMBER_FORM) {
                        scanType = "ISBN"
                    } else if (hmsScan.getScanTypeForm() == HmsScan.ARTICLE_NUMBER_FORM) {
                        scanType = "Product"
                    }
                } else if (hmsScan.getScanType() == HmsScan.EAN8_SCAN_TYPE || hmsScan.getScanType() == HmsScan.UPCCODE_A_SCAN_TYPE || hmsScan.getScanType() == HmsScan.UPCCODE_E_SCAN_TYPE) {
                    if (hmsScan.getScanTypeForm() == HmsScan.ARTICLE_NUMBER_FORM) {
                        scanType = "Product"
                    }
                }

                try {
                    val obj = JSONObject().apply {
                        put("originalValue", originalValue)
                        put("codeFormat", codeFormat)
                        put("scanType", scanType)
                    }
                    val map = jsonObjectToMap(obj)
                    resultToReturn?.success(map)
                    resultToReturn = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }

//                Intent intent = new Intent(this, DisPlayActivity.class);
//                intent.putExtra(RESULT, obj);
//                startActivity(intent);
            }
        }
        return false
    }

    private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = jsonObject.get(key)
        }
        return map
    }
}