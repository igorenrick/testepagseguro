package com.example.testepagseguro;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.listeners.PlugPagAbortListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.listeners.PlugPagActivationListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.listeners.PlugPagPaymentListener;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.MethodChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.FlutterEngine;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import java.util.Objects;
import android.content.Context;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "plugpag_payment";
    Context context = this;

    // Cria a identificação do aplicativo
    PlugPagAppIdentification appIdentification = new PlugPagAppIdentification("testepagseguro", "1.0.0+1");

    // Cria a referência do PlugPag
    PlugPag plugpag = new PlugPag(context, appIdentification);

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            Executor executor = Executors.newSingleThreadExecutor();
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (call.method.equals("abortPayment")) {
                                        PlugPagAbortListener plugPagAbortListener = new PlugPagAbortListener() {
                                            @Override
                                            public void onAbortRequested(boolean b) {
                                                MethodChannel channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
                                                channel.invokeMethod("statusUpdate", "ABORTAR SOLICITAÇÃO");
                                            }

                                            @Override
                                            public void onError(@NonNull String s) {
                                                MethodChannel channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
                                                channel.invokeMethod("statusUpdate", "FALHA AO ABORTAR");
                                            }
                                        };
                                        plugpag.asyncAbort(plugPagAbortListener);
                                    } else
                                    if (call.method.equals("makePayment")) {
                                        String type = call.argument("type");
                                        double amount = call.argument("amount");
                                        String reference = call.argument("reference");
                                        String activationCode = call.argument("activationCode");

                                        //Executor executor = Executors.newSingleThreadExecutor();
                                        //executor.execute(new Runnable() {
                                            //@Override
                                            //public void run() {
                                                // Define os dados do pagamento
                                                PlugPagPaymentData paymentData = new PlugPagPaymentData(
                                                        Objects.equals(type, "credito") ? PlugPag.TYPE_CREDITO : Objects.equals(type, "debito") ? PlugPag.TYPE_DEBITO : 5, //PlugPag.TYPE_PIX,
                                                        (int)(amount * 100),
                                                        PlugPag.INSTALLMENT_TYPE_A_VISTA,
                                                        1,
                                                        reference
                                                );

                                                if(Objects.equals(type, "pix")) {
                                                    assert activationCode != null;
                                                    PlugPagInitializationResult plugPagInitializationResult = plugpag.initializeAndActivatePinpad(new PlugPagActivationData(activationCode));
                                                    if(plugPagInitializationResult.getResult() == PlugPag.RET_OK) {
                                                        PlugPagTransactionResult transactionResult = plugpag.doPayment(paymentData);
                                                        if(transactionResult.getResult() == PlugPag.RET_OK) {
                                                            result.success(transactionResult.getResult() + "," + transactionResult.getMessage());
                                                        } else {
                                                            result.error(Objects.requireNonNull(transactionResult.getErrorCode()),  transactionResult.getResult() + "," +  transactionResult.getMessage(), null);
                                                        }
                                                    }

                                                } else {
                                                    // Ativa terminal e faz o pagamento
                                                    PlugPagActivationListener activationListener = new PlugPagActivationListener() {
                                                        @Override
                                                        public void onActivationProgress(@NonNull PlugPagEventData plugPagEventData) {
                                                            String status = plugPagEventData.getEventCode() + "," + plugPagEventData.getCustomMessage();
                                                            System.out.println(status);
                                                            MethodChannel channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
                                                            channel.invokeMethod("statusUpdate", status);
                                                        }

                                                        @Override
                                                        public void onSuccess(@NonNull PlugPagInitializationResult plugPagInitializationResult) {
                                                            PlugPagPaymentListener listener = new PlugPagPaymentListener() {
                                                                @Override
                                                                public void onError(PlugPagTransactionResult transactionResult) {
                                                                    // Tratar erro na transação
                                                                    result.error(Objects.requireNonNull(transactionResult.getErrorCode()), transactionResult.getResult() + "," + transactionResult.getMessage(), null);
                                                                }

                                                                @Override
                                                                public void onPaymentProgress(PlugPagEventData eventData) {
                                                                    // Tratar o progresso da transação
                                                                    String status = eventData.getEventCode() + "," + eventData.getCustomMessage();
                                                                    System.out.println(status);
                                                                    MethodChannel channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
                                                                    channel.invokeMethod("statusUpdate", status);
                                                                    //result.success("EVENT DATA RESULT:" + eventData.getEventCode() + "EVENT DATA MESSAGE:" + eventData.getCustomMessage());
                                                                }

                                                                @Override
                                                                public void onPrinterError(PlugPagPrintResult printerResult) {
                                                                    // Tratar erro na impressão
                                                                    result.error(Objects.requireNonNull(printerResult.getErrorCode()), printerResult.getResult()  + "," + printerResult.getMessage(), null);
                                                                }

                                                                @Override
                                                                public void onPrinterSuccess(PlugPagPrintResult printerResult) {
                                                                    // Tratar sucesso na impressão
                                                                    result.success(printerResult.getResult() + "," + printerResult.getMessage());
                                                                }

                                                                @Override
                                                                public void onSuccess(PlugPagTransactionResult transactionResult) {
                                                                    // Tratar sucesso na transação
                                                                    String status = transactionResult.getResult() + "," + transactionResult.getMessage();
                                                                    System.out.println(status);
                                                                    MethodChannel channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
                                                                    channel.invokeMethod("statusUpdate", status);
                                                                    //result.success("SUCCESS - TRANSACTION RESULT:" + transactionResult.getResult() + "SUCCESS - TRANSACTION RESULT MESSAGE:" + transactionResult.getMessage());
                                                                }
                                                            };
                                                            plugpag.doAsyncPayment(paymentData, listener);
                                                        }

                                                        @Override
                                                        public void onError(@NonNull PlugPagInitializationResult plugPagInitializationResult) {
                                                            result.error(plugPagInitializationResult.getErrorCode(), plugPagInitializationResult.getResult() + "," + plugPagInitializationResult.getErrorMessage(), null);
                                                        }
                                                    };
                                                    assert activationCode != null;
                                                    plugpag.doAsyncInitializeAndActivatePinpad(new PlugPagActivationData(activationCode), activationListener);
                                                }
                                            //}
                                        //});
                                    } else {
                                        result.notImplemented();
                                    }
                                }
                            });

                        });
    }
}