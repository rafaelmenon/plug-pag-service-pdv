package com.reactlibrary;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.content.pm.PackageInfo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;

public class PlugPagServiceModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private PlugPagAppIdentification appIdentification;
    private PlugPag plugPag;
    private int countPrint = 0;
    private int countImages = 0;
    private String messageCard = null;
    private int countPassword = 0;
    private String getPassword = null;
    private PlugPagTransactionResult result = null;
    private static int lineSpacing = 25;
    private static int fontSize = 16;
    private static int align = 0; //0 = esquerda, 1 = centro, 2 = direita
    private static float sideMarginPercentage = 1; //Em porcentagem
    private static float topMarginPercentage = 1; //Em porcentagem
    private static float bottomMarginPercentage = 2; //Em porcentagem
    private static int width = 340;
    private static boolean bold = false;
    private static String breakLineAuxString;
    private static LinkedList<Piece> pieces;
    private static String fontFamily = "sans-serif";
    private static String lastQrMsg;
    private static Bitmap lastQrImage;



    private PackageInfo getPackageInfo() throws Exception {
        return getReactApplicationContext().getPackageManager().getPackageInfo(getReactApplicationContext().getPackageName(), 0);
    }

    public PlugPagServiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "PlugPagService";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        constants.put("PAYMENT_CREDITO", PlugPag.TYPE_CREDITO);
        constants.put("PAYMENT_DEBITO", PlugPag.TYPE_DEBITO);
        constants.put("PAYMENT_VOUCHER", PlugPag.TYPE_VOUCHER);

        constants.put("INSTALLMENT_TYPE_A_VISTA", PlugPag.INSTALLMENT_TYPE_A_VISTA);
        constants.put("INSTALLMENT_TYPE_PARC_VENDEDOR", PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR);
        constants.put("INSTALLMENT_TYPE_PARC_COMPRADOR", PlugPag.INSTALLMENT_TYPE_PARC_COMPRADOR);

        constants.put("OPERATION_ABORT", PlugPag.OPERATION_ABORT);
        constants.put("OPERATION_ABORTED", PlugPag.OPERATION_ABORTED);
        constants.put("OPERATION_ACTIVATE", PlugPag.OPERATION_ACTIVATE);
        constants.put("OPERATION_CALCULATE_INSTALLMENTS", PlugPag.OPERATION_CALCULATE_INSTALLMENTS);
        constants.put("OPERATION_CHECK_AUTHENTICATION", PlugPag.OPERATION_CHECK_AUTHENTICATION);
        constants.put("OPERATION_DEACTIVATE", PlugPag.OPERATION_DEACTIVATE);
        constants.put("OPERATION_GET_APPLICATION_CODE", PlugPag.OPERATION_GET_APPLICATION_CODE);
        constants.put("OPERATION_GET_LIB_VERSION", PlugPag.OPERATION_GET_LIB_VERSION);
        constants.put("OPERATION_GET_READER_INFOS", PlugPag.OPERATION_GET_READER_INFOS);
        constants.put("OPERATION_GET_USER_DATA", PlugPag.OPERATION_GET_USER_DATA);
        constants.put("OPERATION_HAS_CAPABILITY", PlugPag.OPERATION_HAS_CAPABILITY);
        constants.put("OPERATION_INVALIDATE_AUTHENTICATION", PlugPag.OPERATION_INVALIDATE_AUTHENTICATION);
        constants.put("OPERATION_NFC_ABORT", PlugPag.OPERATION_NFC_ABORT);
        constants.put("OPERATION_NFC_READ", PlugPag.OPERATION_NFC_READ);
        constants.put("OPERATION_NFC_WRITE", PlugPag.OPERATION_NFC_WRITE);
        constants.put("OPERATION_PAYMENT", PlugPag.OPERATION_PAYMENT);
        constants.put("OPERATION_PRINT", PlugPag.OPERATION_PRINT);
        constants.put("OPERATION_QUERY_LAST_APPROVED_TRANSACTION", PlugPag.OPERATION_QUERY_LAST_APPROVED_TRANSACTION);
        constants.put("OPERATION_REFUND", PlugPag.OPERATION_REFUND);
        constants.put("OPERATION_REPRINT_CUSTOMER_RECEIPT", PlugPag.OPERATION_REPRINT_CUSTOMER_RECEIPT);

        constants.put("ACTION_POST_OPERATION", PlugPag.ACTION_POST_OPERATION);
        constants.put("ACTION_PRE_OPERATION", PlugPag.ACTION_PRE_OPERATION);
        constants.put("ACTION_UPDATE", PlugPag.ACTION_UPDATE);

        constants.put("APN_SERVICE_CLASS_NAME", PlugPag.APN_SERVICE_CLASS_NAME);
        constants.put("APN_SERVICE_PACKAGE_NAME", PlugPag.APN_SERVICE_PACKAGE_NAME);

        constants.put("AUTHENTICATION_FAILED", PlugPag.AUTHENTICATION_FAILED);
        constants.put("COMMUNICATION_ERROR", PlugPag.COMMUNICATION_ERROR);
        constants.put("ERROR_CODE_OK", PlugPag.ERROR_CODE_OK);
        constants.put("MIN_PRINTER_STEPS", PlugPag.MIN_PRINTER_STEPS);
        constants.put("NFC_SERVICE_CLASS_NAME", PlugPag.NFC_SERVICE_CLASS_NAME);
        constants.put("NFC_SERVICE_PACKAGE_NAME", PlugPag.NFC_SERVICE_PACKAGE_NAME);
        constants.put("NO_PRINTER_DEVICE", PlugPag.NO_PRINTER_DEVICE);
        constants.put("NO_TRANSACTION_DATA", PlugPag.NO_TRANSACTION_DATA);
        constants.put("SERVICE_CLASS_NAME", PlugPag.SERVICE_CLASS_NAME);
        constants.put("SERVICE_PACKAGE_NAME", PlugPag.SERVICE_PACKAGE_NAME);
        constants.put("SMART_RECHARGE_SERVICE_CLASS_NAME", PlugPag.SMART_RECHARGE_SERVICE_CLASS_NAME);
        constants.put("SMART_RECHARGE_SERVICE_PACKAGE_NAME", PlugPag.SMART_RECHARGE_SERVICE_PACKAGE_NAME);

        constants.put("RET_OK", PlugPag.RET_OK);
        String appVersion;

        try {
            appVersion = getPackageInfo().versionName;
        } catch (Exception e) {
            appVersion = "unkown";
        }
        constants.put("appVersion", appVersion);
        return constants;
    }

    // Cria a identificação do aplicativo
    @ReactMethod
    public void setAppIdendification(String name, String version) {
        appIdentification = new PlugPagAppIdentification(name, version);
        plugPag = new PlugPag(reactContext, appIdentification);
    }

    // Ativa terminal e faz o pagamento
    @ReactMethod
    public void initializeAndActivatePinpad(String activationCode, Promise promise) {
        final PlugPagActivationData activationData = new PlugPagActivationData(activationCode);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<PlugPagInitializationResult> callable = new Callable<PlugPagInitializationResult>() {
            @Override
            public PlugPagInitializationResult call() throws Exception {
                return plugPag.initializeAndActivatePinpad(activationData);
            }
        };

        Future<PlugPagInitializationResult> future = executor.submit(callable);
        executor.shutdown();

        try {
            PlugPagInitializationResult initResult = future.get();

            final WritableMap map = Arguments.createMap();
            map.putInt("retCode", initResult.getResult());

            promise.resolve(map);
        } catch (ExecutionException e) {
            Log.d("PlugPag", e.getMessage());
            promise.reject("error", e.getMessage());
        } catch (InterruptedException e) {
            Log.d("PlugPag", e.getMessage());
            promise.reject("error", e.getMessage());
        }
    }

    private static class Piece {
        protected int height;
        protected Paint paint;

        public int getHeight() {
            return height;
        }

        public Paint getPaint() {
            return paint;
        }
    }

    public static void initNewImage() {
        lineSpacing = 25;
        fontSize = 250;
        align = 0;
        sideMarginPercentage = 0;
        topMarginPercentage = 1;
        bottomMarginPercentage = 2;
        width = 1155;
        bold = false;
        breakLineAuxString = null;

        pieces = new LinkedList<Piece>();
    }

    public static void setBold(boolean _bold) {
        bold = _bold;
    }

    public static void setLineSpacing(int _lineSpacing) {
        lineSpacing = _lineSpacing;
    }

    public static void setFontSize(int _fontSize) {
        fontSize = _fontSize;
    }

    public static void setFontFamily(String _fontFamily) {
        fontFamily = _fontFamily;
    }

    public static void setAlign(int _align) {
        align = _align;
    }

    public static void setSideMarginPercentage(float _sideMarginPercentage) {
        sideMarginPercentage = _sideMarginPercentage;
    }

    public static void setTopMarginPercentage(float _topMarginPercentage) {
        topMarginPercentage = _topMarginPercentage;
    }

    public static void setBottomMarginPercentage(float _bottomMarginPercentage) {
        bottomMarginPercentage = _bottomMarginPercentage;
    }

    public static void setImageWidth(int _width) {
        width = _width;
    }

    private static class TextPiece extends Piece {
        private String text;

        public TextPiece(String _text) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setTextSize(fontSize);
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
            paint.setFakeBoldText(bold);

            if (align == 1) {
                paint.setTextAlign(Paint.Align.CENTER);
            } else if (align == 2) {
                paint.setTextAlign(Paint.Align.RIGHT);
            } else {
                paint.setTextAlign(Paint.Align.LEFT);
            }

            //Calcula se cabe o texto completo sem quebra linha
            int charactersAmount = paint.breakText(_text, true, width, null);

            if (charactersAmount < _text.length()) {
                breakLineAuxString = _text.substring(charactersAmount);
                _text = _text.substring(0, charactersAmount);
            } else {
                breakLineAuxString = null;
            }

            text = _text;

            Rect rectText = new Rect();
            paint.getTextBounds(text, 0, text.length(), rectText);
            height = rectText.height() + lineSpacing;
        }

        public String getText() {
            return text;
        }
    }

    public static void addTextLine(String text) {
        pieces.add(new TextPiece(text));

        while (breakLineAuxString != null && !breakLineAuxString.isEmpty()) {
            pieces.add(new TextPiece(breakLineAuxString));
        }
    }

    private static class ImagePiece extends Piece {
        private Bitmap image;

        public ImagePiece(byte[] _imageBytes) {
            SetImage(BitmapFactory.decodeByteArray(_imageBytes, 0, _imageBytes.length));
        }

        public ImagePiece(Bitmap _image) {
            SetImage(_image);
        }

        private void SetImage (Bitmap _image) {
            image = _image;
            height = image.getHeight() + lineSpacing;

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            if (align == 1) {
                paint.setTextAlign(Paint.Align.CENTER);
            } else if (align == 2) {
                paint.setTextAlign(Paint.Align.RIGHT);
            } else {
                paint.setTextAlign(Paint.Align.LEFT);
            }
        }

        public Bitmap getImage() {
            return image;
        }
    }

    public static void addImage(byte[] imageBytes) {
        pieces.add(new ImagePiece(imageBytes));
    }

    public static void addImage(Bitmap image) {
        pieces.add(new ImagePiece(image));
    }

    public static void generateImage(String fileName) {
        Bitmap newBitmap = null;

        try {
            int _sideMargin = (int) (width * sideMarginPercentage) / 100;
            int _bottomMargin = (int) (width * bottomMarginPercentage) / 100;
            int _topMargin = (int) (width * topMarginPercentage) / 100;

            Iterator iterator = pieces.iterator();
            int height = ((Piece) iterator.next()).getHeight() + _topMargin + _bottomMargin;
            while (iterator.hasNext()) {
                height += ((Piece) iterator.next()).getHeight();
            }

            newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(newBitmap);

            Paint paint2 = new Paint();
            paint2.setColor(Color.WHITE);
            paint2.setStyle(Paint.Style.FILL);
            newCanvas.drawPaint(paint2);

            iterator = pieces.iterator();
            int currentHeight = _topMargin;
            while (iterator.hasNext()) {
                Piece currentPiece = ((Piece) iterator.next());

                int x = _sideMargin;
                if (currentPiece.getPaint().getTextAlign() == Paint.Align.CENTER) {
                    x = width / 2;
                } else if (currentPiece.getPaint().getTextAlign() == Paint.Align.RIGHT) {
                    x = width - _sideMargin;
                }

                if (currentPiece instanceof TextPiece) {
                    currentHeight += currentPiece.getHeight();
                    newCanvas.drawText(((TextPiece) currentPiece).getText(), x, currentHeight, currentPiece.getPaint());
                } else if (currentPiece instanceof ImagePiece) {
                    newCanvas.drawBitmap(((ImagePiece) currentPiece).getImage(), (x - (((ImagePiece) currentPiece).getImage().getWidth() / 2)), currentHeight, currentPiece.getPaint());
                    currentHeight += currentPiece.getHeight();
                }
            }
        } catch (Exception e) {

        }

        try {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print/");
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, fileName);
            FileOutputStream fOut = new FileOutputStream(file);

            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public String returnNumberFi(Integer total, Integer actual) {
        String newValue = actual + "/" + total;
        return newValue;
    }

    public static Bitmap generateQrCode(String codeMsg, int size) {
        if (!codeMsg.equals(lastQrMsg)) {
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();

                BitMatrix bitMatrix = qrCodeWriter.encode(codeMsg, BarcodeFormat.QR_CODE, size, size);
                Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

                for (Integer x = 0; x < size; x++) {
                    for (Integer y = 0; y < size; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                lastQrMsg = codeMsg;
                lastQrImage = bitmap;

                return bitmap;
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else {
            return lastQrImage;
        }

        return null;
    }

    @ReactMethod
    public void printGrouped(String jsonStr, final Promise promise) throws JSONException {
        final JSONObject jsonObject = new JSONObject(jsonStr);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final JSONArray products = jsonObject.getJSONArray("products");
        final Bitmap image = BitmapFactory.decodeFile((String) jsonObject.get("logo_path"));

        Runnable runnableTask = new Runnable() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void run() {
                setAppIdendification("pdv365", "0.0.1");
                initNewImage();
                setBold(true);
                setFontSize(128);
                setAlign(1);
                addImage(Bitmap.createScaledBitmap(image, 1000, 400, false));
                for(int i = 0; i < products.length(); i++){
                    JSONObject o = null;
                    try {
                        o = products.getJSONObject(i);
                        setFontSize(80);
                        addTextLine((Integer) o.get("quantity") + " " +((String) o.get("name")).toUpperCase());
                        setLineSpacing(60);
                        if (o.getJSONArray("additional").length() > 0) {
                            setBold(true);
                            setFontSize(50);
                            setLineSpacing(50);
                            addTextLine("ADICIONAIS");
                            JSONObject x = null;

                            for(int a = 0; a < o.getJSONArray("additional").length(); a++) {
                                x = o.getJSONArray("additional").getJSONObject(a);
                                setLineSpacing(25);
                                addTextLine(+(Integer) x.get("quantity") + "    " + ((String) x.get("name")).toUpperCase() + "   " +   ((String) x.get("value")).toUpperCase());
                                setFontSize(30);
                                addTextLine("_____________________________________________");
                                setFontSize(50);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                setBold(true);

                try {
                    if ((boolean) jsonObject.get("print_qr_code") == true) {
                        addImage(generateQrCode((String) jsonObject.get("sale_id"), 600));
                    }
                    if ((boolean) jsonObject.get("is_reprint") == true) {
                        setBold(true);
                        setLineSpacing(80);
                        addTextLine("REIMPRESSÃO");
                    }
                    setFontSize(128);
                    addTextLine((String) jsonObject.get("sale_total"));
                    setLineSpacing(140);
                    setBold(false);
                    setFontSize(60);
                    addTextLine(((String) jsonObject.get("event_name")).toUpperCase());
                    setLineSpacing(25);
                    addTextLine(((String) jsonObject.get("event_sector_name")).toUpperCase());
                    setLineSpacing(90);
                    if ((boolean) jsonObject.get("is_waiter_sale") == true) {
                        setBold(true);
                        setLineSpacing(80);
                        addTextLine("GARÇOM");
                        setLineSpacing(25);
                        addTextLine(((String) jsonObject.get("waiter_name")).toUpperCase());
                    }
                    setBold(false);
                    setLineSpacing(80);
                    addTextLine(((String) jsonObject.get("operator_name") + " " + "-" + " " + (String) jsonObject.get("serial")).toUpperCase());
                    setLineSpacing(25);
                    addTextLine(((String) jsonObject.get("sale_date")).toUpperCase());
                    setLineSpacing(60);
                    setFontSize(30);
                    setBold(false);
                    addTextLine("__________________________________________");
                    setFontSize(60);
                    addTextLine("RECORTE AQUI");
                    setFontSize(30);
                    addTextLine("__________________________________________");

                    if (jsonObject.getJSONArray("production_products").length() > 0) {
                        setBold(true);
                        setFontSize(95);
                        addImage(Bitmap.createScaledBitmap(image, 1000, 400, false));
                        addTextLine("FICHA DE PRODUÇÃO");
                        setFontSize(30);
                        addTextLine("__________________________________________");
                        setLineSpacing(60);

                        for(int p = 0; p < jsonObject.getJSONArray("production_products").length(); p++){
                            JSONObject prodG = null;
                            try {
                                prodG = jsonObject.getJSONArray("production_products").getJSONObject(p);
                                setFontSize(80);
                                addTextLine(+(Integer) prodG.get("quantity") + "    " + ((String) prodG.get("name")).toUpperCase());
                                if (prodG.getJSONArray("additional").length() > 0) {
                                    setBold(true);
                                    setFontSize(50);
                                    setLineSpacing(50);
                                    addTextLine("ADICIONAIS");
                                    JSONObject x = null;

                                    for(int a = 0; a < prodG.getJSONArray("additional").length(); a++) {
                                        x = prodG.getJSONArray("additional").getJSONObject(a);
                                        setLineSpacing(25);
                                        addTextLine(+(Integer) x.get("quantity") + "    " + ((String) x.get("name")).toUpperCase() + "   " +   ((String) x.get("value")).toUpperCase());
                                        setFontSize(30);
                                        addTextLine("_____________________________________________");
                                        setFontSize(50);
                                    }
                                }
                                setAlign(1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        setFontSize(110);
                        addTextLine(((String) jsonObject.get("production_total")).toUpperCase());
                        setFontSize(60);
                        setBold(false);
                        setLineSpacing(80);
                        addTextLine(((String) jsonObject.get("operator_name") + " " + "-" + " " + (String) jsonObject.get("serial")).toUpperCase());
                        setLineSpacing(25);
                        addTextLine(((String) jsonObject.get("sale_date")).toUpperCase());
                        setLineSpacing(60);
                        setFontSize(30);
                        setBold(false);
                        addTextLine("__________________________________________");
                        setFontSize(60);
                        addTextLine("RECORTE AQUI");
                        setFontSize(30);
                        addTextLine("__________________________________________");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                generateImage("imagem");
                countPrint = 0;
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print");
                File[] arquivos = file.listFiles();
                countImages = arquivos.length;
                for (File fileTmp : arquivos) {
                    final PlugPagPrinterData data = new PlugPagPrinterData( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print/" + fileTmp.getName(), 4, 10 * 12);
                    PlugPagPrinterListener listener = new PlugPagPrinterListener() {
                        @Override
                        public void onError(PlugPagPrintResult plugPagPrintResult) {
                            promise.reject("error", plugPagPrintResult.getMessage());
                        }
                        @Override
                        public void onSuccess(PlugPagPrintResult plugPagPrintResult) {
                            countPrint++;
                            if (countPrint == countImages) {
                                promise.resolve(null);
                            }
                        }
                    };
                    plugPag.setPrinterListener(listener);
                    plugPag.printFromFile(data);
                    fileTmp.delete();
                    System.gc();
                }
            }
        };
        executor.execute(runnableTask);
        executor.shutdown();
        System.gc();
    }

    @ReactMethod
    public void printProduction(String jsonStr, final Promise promise) throws JSONException {
        final JSONObject jsonObject = new JSONObject(jsonStr);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final JSONArray products = jsonObject.getJSONArray("products");
        final Bitmap image = BitmapFactory.decodeFile((String) jsonObject.get("logo_path"));

        Runnable runnableTask = new Runnable() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void run() {
                setAppIdendification("pdv365", "0.0.1");
                initNewImage();
                for(int i = 0; i < products.length(); i++){
                    JSONObject o = null;
                    try {
                        o = products.getJSONObject(i);
                        setBold(true);
                        setFontSize(128);
                        setAlign(1);
                        addImage(Bitmap.createScaledBitmap(image, 1000, 400, false));
                        addTextLine(((String) o.get("name")).toUpperCase());
                        if (o.getJSONArray("additional").length() > 0) {
                            setBold(true);
                            setFontSize(50);
                            setLineSpacing(50);
                            addTextLine("ADICIONAIS");
                            JSONObject x = null;
                            for(int a = 0; a < o.getJSONArray("additional").length(); a++) {
                                x = o.getJSONArray("additional").getJSONObject(a);
                                setLineSpacing(25);
                                addTextLine(+(Integer) x.get("quantity") + "    " + ((String) x.get("name")).toUpperCase() + "   " +   ((String) x.get("value")).toUpperCase());
                                setFontSize(30);
                                addTextLine("___________________________________________");
                                setFontSize(50);
                            }
                        }
                        setFontSize(128);
                        addTextLine(((String) o.get("final_value")).toUpperCase());
                        if ((boolean) jsonObject.get("print_qr_code") == true) {
                            addImage(generateQrCode((String) o.get("uuid"), 600));
                        }
                        if ((boolean) o.get("is_reprint") == true) {
                            setBold(true);
                            setLineSpacing(80);
                            addTextLine("REIMPRESSÃO");
                        }
                        setLineSpacing(140);
                        setBold(false);
                        setFontSize(60);
                        addTextLine(((String) jsonObject.get("event_name")).toUpperCase());
                        setLineSpacing(25);
                        addTextLine(((String) jsonObject.get("event_sector_name")).toUpperCase());
                        if ((boolean) o.get("has_production_sheet") == true) {
                            setBold(false);
                            setLineSpacing(60);
                            addTextLine("_________________________________");
                            setBold(true);
                            addTextLine("SENHA");
                            setFontSize(128);
                            addTextLine(((String) jsonObject.get("production_password")).toUpperCase());
                            setFontSize(60);
                            setBold(false);
                            addTextLine("_________________________________");
                        }
                        setLineSpacing(90);
                        if ((boolean) jsonObject.get("is_waiter_sale") == true) {
                            setBold(true);
                            setLineSpacing(80);
                            addTextLine("GARÇOM");
                            setLineSpacing(25);
                            addTextLine(((String) jsonObject.get("waiter_name")).toUpperCase());
                        }
                        setBold(false);
                        setLineSpacing(80);
                        addTextLine(returnNumberFi(products.length(), i + 1));
                        setBold(true);
                        addTextLine(((String) jsonObject.get("operator_name") + " " + "-" + " " + (String) jsonObject.get("serial")).toUpperCase());
                        setLineSpacing(25);
                        addTextLine(((String) jsonObject.get("sale_date")).toUpperCase());
                        setLineSpacing(60);
                        setFontSize(30);
                        setBold(false);
                        addTextLine("__________________________________________");
                        setFontSize(60);
                        addTextLine("RECORTE AQUI");
                        setFontSize(30);
                        addTextLine("__________________________________________");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                setBold(true);
                setFontSize(95);
                addImage(Bitmap.createScaledBitmap(image, 1000, 400, false));
                addTextLine("FICHA DE PRODUÇÃO");
                setFontSize(30);
                addTextLine("__________________________________________");
                setLineSpacing(60);
                JSONObject prod = null;
                for(int p = 0; p < products.length(); p++) {
                    try {
                        prod = jsonObject.getJSONArray("production_products").getJSONObject(p);
                        setLineSpacing(100);
                        setFontSize(80);
                        setAlign(3);
                        addTextLine(+(Integer) prod.get("quantity") + "    " + ((String) prod.get("name")).toUpperCase());
                        if (prod.getJSONArray("additional").length() > 0) {
                            setBold(true);
                            setFontSize(50);
                            setLineSpacing(50);
                            addTextLine("ADICIONAIS");
                            JSONObject x = null;

                            for(int a = 0; a < prod.getJSONArray("additional").length(); a++) {
                                x = prod.getJSONArray("additional").getJSONObject(a);
                                setLineSpacing(25);
                                addTextLine(+(Integer) x.get("quantity") + "    " + ((String) x.get("name")).toUpperCase() + "   " +   ((String) x.get("value")).toUpperCase());
                                setFontSize(30);
                                addTextLine("_____________________________________________");
                                setFontSize(50);
                            }
                        }
                        setAlign(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                setFontSize(128);
                try {
                    addTextLine(((String) jsonObject.get("production_total")).toUpperCase());
                    setFontSize(60);
                    setBold(false);
                    setLineSpacing(110);
                    setBold(true);
                    addTextLine(((String) jsonObject.get("operator_name") + " " + "-" + " " + (String) jsonObject.get("serial")).toUpperCase());
                    setLineSpacing(25);
                    addTextLine(((String) jsonObject.get("sale_date")).toUpperCase());
                    setLineSpacing(60);
                    setFontSize(30);
                    setBold(false);
                    addTextLine("_______________________________________________");
                    setFontSize(60);
                    addTextLine("RECORTE AQUI");
                    setFontSize(30);
                    addTextLine("_______________________________________________");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                generateImage("imagem");
                countPrint = 0;
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print");
                File[] arquivos = file.listFiles();
                countImages = arquivos.length;
                for (File fileTmp : arquivos) {
                    final PlugPagPrinterData data = new PlugPagPrinterData( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print/" + fileTmp.getName(), 4, 10 * 12);
                    PlugPagPrinterListener listener = new PlugPagPrinterListener() {
                        @Override
                        public void onError(PlugPagPrintResult plugPagPrintResult) {
                            promise.reject("error", plugPagPrintResult.getMessage());
                        }
                        @Override
                        public void onSuccess(PlugPagPrintResult plugPagPrintResult) {
                            countPrint++;
                            if (countPrint == countImages) {
                                promise.resolve(null);
                            }
                        }
                    };
                    plugPag.setPrinterListener(listener);
                    plugPag.printFromFile(data);
                    fileTmp.delete();
                    System.gc();
                }
            }
        };
        executor.execute(runnableTask);
        executor.shutdown();
        System.gc();
    }

    @ReactMethod
    public void ImageAndPrint(String jsonStr, final Promise promise) throws JSONException {
        final JSONObject jsonObject = new JSONObject(jsonStr);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final JSONArray products = jsonObject.getJSONArray("products");

        final Bitmap image = BitmapFactory.decodeFile((String) jsonObject.get("logo_path"));

        Runnable runnableTask = new Runnable() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void run() {
                setAppIdendification("pdv365", "0.0.1");
                initNewImage();
                for(int i = 0; i < products.length(); i++){
                    JSONObject o = null;
                    try {
                        o = products.getJSONObject(i);
                        setBold(true);
                        setFontSize(128);
                        setAlign(1);
                        addImage(Bitmap.createScaledBitmap(image, 1000, 400, false));
                        addTextLine(((String) o.get("name")).toUpperCase());
                        if (o.getJSONArray("additional").length() > 0) {
                            setBold(true);
                            setFontSize(50);
                            setLineSpacing(50);
                            addTextLine("ADICIONAIS");
                            JSONObject x = null;

                            for(int a = 0; a < o.getJSONArray("additional").length(); a++) {
                                x = o.getJSONArray("additional").getJSONObject(a);
                                setLineSpacing(25);
                                addTextLine(+(Integer) x.get("quantity") + "    " + ((String) x.get("name")).toUpperCase() + "   " +   ((String) x.get("value")).toUpperCase());
                                setFontSize(30);
                                addTextLine("__________________________________________________________");
                                setFontSize(50);
                            }
                        }
                        setFontSize(128);
                        addTextLine(((String) o.get("final_value")).toUpperCase());
                        if ((boolean) jsonObject.get("print_qr_code") == true) {
                            addImage(generateQrCode((String) o.get("uuid"), 600));
                        }
                        if ((boolean) o.get("is_reprint") == true) {
                            setBold(true);
                            setLineSpacing(110);
                            addTextLine("REIMPRESSÃO");
                        }
                        setLineSpacing(80);
                        setBold(false);
                        setFontSize(60);
                        addTextLine(((String) jsonObject.get("event_name")).toUpperCase());
                        setLineSpacing(25);
                        addTextLine(((String) jsonObject.get("event_sector_name")).toUpperCase());
                        setLineSpacing(80);
                        if ((boolean) jsonObject.get("is_waiter_sale") == true) {
                            setBold(true);
                            setLineSpacing(80);
                            addTextLine("GARÇOM");
                            setLineSpacing(25);
                            addTextLine(((String) jsonObject.get("waiter_name")).toUpperCase());
                        }
                        setBold(false);
                        setLineSpacing(80);
                        addTextLine(returnNumberFi(products.length(), i + 1));
                        setBold(true);
                        addTextLine(((String) jsonObject.get("operator_name") + " " + "-" + " " + (String) jsonObject.get("serial")).toUpperCase());
                        setLineSpacing(25);
                        addTextLine(((String) jsonObject.get("sale_date")).toUpperCase());
                        setLineSpacing(40);
                        setFontSize(30);
                        setBold(false);
                        addTextLine("__________________________________________________________");
                        setFontSize(60);
                        addTextLine("RECORTE AQUI");
                        setFontSize(30);
                        addTextLine("__________________________________________________________");
                        setLineSpacing(100);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                generateImage("imagem");
                countPrint = 0;
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print");
                File[] arquivos = file.listFiles();
                countImages = arquivos.length;

                for (File fileTmp : arquivos) {
                    final PlugPagPrinterData data = new PlugPagPrinterData( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print/" + fileTmp.getName(), 4, 10 * 12);
                    PlugPagPrinterListener listener = new PlugPagPrinterListener() {
                        @Override
                        public void onError(PlugPagPrintResult plugPagPrintResult) {
                            promise.reject("error", plugPagPrintResult.getMessage());
                        }

                        @Override
                        public void onSuccess(PlugPagPrintResult plugPagPrintResult) {
                            countPrint++;
                            if (countPrint == countImages) {
                                promise.resolve(null);
                            }
                        }
                    };
                    plugPag.setPrinterListener(listener);
                    plugPag.printFromFile(data);
                    fileTmp.delete();
                    System.gc();
                }
            }
        };
        executor.execute(runnableTask);
        executor.shutdown();
        System.gc();
    }

    @ReactMethod
    public void doPayment(String jsonStr, final Promise promise) {
        final PlugPagPaymentData paymentData = JsonParseUtils.getPlugPagPaymentDataFromJson(jsonStr);

        plugPag.setEventListener(new PlugPagEventListener() {
            @Override
            public void onEvent(final PlugPagEventData plugPagEventData) {
                messageCard = plugPagEventData.getCustomMessage();
                int code = plugPagEventData.getEventCode();

                if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD || plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
                    if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                        countPassword++;
                    } else if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
                        countPassword = 0;
                    }

                    if (countPassword == 0 ) {
                        getPassword = "Senha:";
                    } else if (countPassword == 1) {
                        getPassword = "Senha: *";
                    } else if (countPassword == 2) {
                        getPassword = "Senha: **";
                    } else if (countPassword == 3) {
                        getPassword = "Senha: ***";
                    } else if (countPassword == 4) {
                        getPassword = "Senha: ****";
                    } else if (countPassword == 5) {
                        getPassword = "Senha: *****";
                    } else if (countPassword == 6 || countPassword > 6) {
                        getPassword = "Senha: ******";
                    }

                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", getPassword);
                } else {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                }
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                PlugPagTransactionResult transactionResult = plugPag.doPayment(paymentData);
                final WritableMap map = Arguments.createMap();
                map.putInt("retCode", transactionResult.getResult());
                map.putString("transactionCode", transactionResult.getTransactionCode());
                map.putString("transactionId", transactionResult.getTransactionId());
                promise.resolve(map);
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }

    @ReactMethod
    public void reprintStablishmentReceipt(Promise promise) {
        PlugPagPrintResult result = plugPag.reprintStablishmentReceipt();
        promise.resolve(result.getResult());
    }

    @ReactMethod
    public void reprintCustomerReceipt(Promise promise) {
        PlugPagPrintResult result = plugPag.reprintCustomerReceipt();
        promise.resolve(result.getResult());
    }

    @ReactMethod
    public void reversePayment(final String code, final String id, final Promise promise) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                PlugPagTransactionResult transactionResult = plugPag.voidPayment(new PlugPagVoidData(code, id, true));
                final WritableMap map = Arguments.createMap();
                map.putInt("retCode", transactionResult.getResult());
                map.putString("message", transactionResult.getMessage());
                promise.resolve(map);
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }

    /*Método para pegar o serial do pos*/
    @ReactMethod
    public void getSerialNumber(Promise promise) throws NoSuchFieldException, IllegalAccessException {
        String deviceSerial = (String) Build.class.getField("SERIAL").get(null);
        promise.resolve(deviceSerial);
    }

    /* Método para ler ID do cartão*/
    @ReactMethod
    public void readNFCCardClean(final int slot, final Promise promise) throws UnsupportedEncodingException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
                dataCard.setStartSlot(slot);
                dataCard.setEndSlot(slot);
                PlugPagNFCResult result = plugPag.readFromNFCCard(dataCard);
                String returnValue = new String(result.getSlots()[result.getStartSlot()].get("data"), StandardCharsets.UTF_8);

                if (result.getResult() == -1) {
                    promise.resolve(null);
                } else {
                    promise.resolve(returnValue);
                }
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }

    /* Método para escrever ID no cartão*/
    @ReactMethod
    public void writeToNFCCardClean(final int slot, final String info, final Promise promise) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                byte[] bytes = info.getBytes();

                PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
                dataCard.setStartSlot(slot);
                dataCard.setEndSlot(slot);
                dataCard.getSlots()[slot].put("data", bytes);

                PlugPagNFCResult result = plugPag.writeToNFCCard(dataCard);
                int returnResult = result.getResult();
                promise.resolve(returnResult);
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }
    /* Método para ler qualquer slot do cartão com hash*/
    @ReactMethod
    public void readNFCCard(int slot, Promise promise) throws UnsupportedEncodingException {
        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        PlugPagNFCResult result = plugPag.readFromNFCCard(dataCard);
        String returnValue = new String(result.getSlots()[result.getStartSlot()].get("data"), "UTF-8");
        byte[] data2 = Base64.decode(returnValue, Base64.DEFAULT);
        String text = new String(data2, StandardCharsets.UTF_8);
        promise.resolve(text);
    }

    /* Método para escrever qualquer slot do cartão com hash*/
    @ReactMethod
    public void writeToNFCCard(int slot, String info, Promise promise) throws UnsupportedEncodingException {
        byte[] bytes = info.getBytes("UTF-8");
        String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        byte[] bytesWrite = base64.getBytes("UTF-8");

        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        dataCard.getSlots()[slot].put("data", bytesWrite);

        PlugPagNFCResult result = plugPag.writeToNFCCard(dataCard);
        int returnResult = result.getResult();
        promise.resolve(returnResult);
    }

    @ReactMethod
    public void cancelOperation() {
        plugPag.abort();
    }

    @ReactMethod
    public void printFile(final Promise promise) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                setAppIdendification("pdv365", "0.0.1");
                countPrint = 0;
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print");
                File[] arquivos = file.listFiles();
                countImages = arquivos.length;


                for (File fileTmp : arquivos) {

                    final PlugPagPrinterData data = new PlugPagPrinterData( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/print/" + fileTmp.getName(), 4, 10 * 12);

                    PlugPagPrinterListener listener = new PlugPagPrinterListener() {
                        @Override
                        public void onError(PlugPagPrintResult plugPagPrintResult) {
                            promise.reject("error", plugPagPrintResult.getMessage());
                        }

                        @Override
                        public void onSuccess(PlugPagPrintResult plugPagPrintResult) {
                            countPrint++;
                            if (countPrint == countImages) {
                                promise.resolve(null);
                            }
                        }
                    };

                    plugPag.setPrinterListener(listener);

                    plugPag.printFromFile(data);
                    fileTmp.delete();
                }
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable boolean params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("connectionEvent", params);
    }

    @ReactMethod
    public void connection() {
        ConnectivityManager conn = (ConnectivityManager)reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conn.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null ? activeNetwork.isConnectedOrConnecting() : false;

        sendEvent(reactContext, "connectionEvent", isConnected);
    }

    @ReactMethod
    public void cancelReadCard(Promise promise) {
        PlugPagNFCResult result =  plugPag.abortNFC();

        if (result.getResult() == -1) {
            promise.reject(null, "Não foi possível cancelar a operação.");
        } else {
            promise.resolve(null);
        }
    }
}
