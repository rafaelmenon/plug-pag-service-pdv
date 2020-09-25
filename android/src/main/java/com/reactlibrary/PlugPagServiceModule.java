package com.reactlibrary;

import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;

public class PlugPagServiceModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private PlugPagAppIdentification appIdentification;
    private PlugPag plugPag;

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
        constants.put("OPERATION_REPRINT_STABLISHMENT_RECEIPT", PlugPag.OPERATION_REPRINT_STABLISHMENT_RECEIPT);

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

    @ReactMethod
    public void doPayment(String jsonStr, Promise promise) {
        final PlugPagPaymentData paymentData = JsonParseUtils.getPlugPagPaymentDataFromJson(jsonStr);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<PlugPagTransactionResult> callable = new Callable<PlugPagTransactionResult>() {
            @Override
            public PlugPagTransactionResult call() throws Exception {
                return plugPag.doPayment(paymentData);
            }
        };

        Future<PlugPagTransactionResult> future = executor.submit(callable);
        executor.shutdown();

        try {
            PlugPagTransactionResult transactionResult = future.get();

            final WritableMap map = Arguments.createMap();
            map.putInt("retCode", transactionResult.getResult());

            promise.resolve(map);
        } catch (ExecutionException e) {
            Log.d("PlugPag", e.getMessage());
            promise.reject("error", e.getMessage());
        } catch (InterruptedException e) {
            Log.d("PlugPag", e.getMessage());
            promise.reject("error", e.getMessage());
        }
    }

    /*Método para pegar o serial do pos*/
    @ReactMethod
    public void getSerialNumber(Promise promise) throws NoSuchFieldException, IllegalAccessException {
        String deviceSerial = (String) Build.class.getField("SERIAL").get(null);
        promise.resolve(deviceSerial);
    }

    /* Método temporário para o pos */
    @ReactMethod
    public void temporaryReadNFC(int slot, Promise promise) {
        System.out.println("slot passado ->>>>>" + slot);
        String identification = "4549430934564676";
        promise.resolve(identification);
    }


    /* Método para ler o cartão*/
    @ReactMethod
    public void readNFCCard(int slot, Promise promise) throws UnsupportedEncodingException {
        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        PlugPagNFCResult result = plugPag.readFromNFCCard(dataCard);
        String returnValue = new String(result.getSlots()[result.getStartSlot()].get("data"), "UTF-8");
        promise.resolve(returnValue);
    }

    @ReactMethod
    public void writeToNFCCard(int slot, Promise promise) {
        String info = "teste_com16bytes";
        byte[] infoBytes = info.getBytes();

        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        dataCard.getSlots()[slot].put("data", infoBytes);

        PlugPagNFCResult result = plugPag.writeToNFCCard(dataCard);
        int resultado = result.getResult();
        promise.resolve(resultado);
    }

}
