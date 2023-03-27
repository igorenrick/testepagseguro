/*
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

public class PaymentMethodCallHandler implements MethodCallHandler {
  private final Registrar registrar;

  public PaymentMethodCallHandler(Registrar registrar) {
    this.registrar = registrar;
  }
  
  @Override
  public void onMethodCall(MethodCall call, final MethodChannel.Result result) {
    if (call.method.equals("makePayment")) {
        String type = call.argument("type");
        double amount = call.argument("amount");
        String reference = call.argument("reference");
        
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Aqui você deve chamar o método da biblioteca PlugPag para realizar o pagamento
                // Utilize os parâmetros "type", "amount" e "reference" para realizar a chamada

                // Define os dados do pagamento
                PlugPagPaymentData paymentData = new PlugPagPaymentData(
                    Objects.equals(type, "credito") ? PlugPag.TYPE_CREDITO : Objects.equals(type, "debito") ? PlugPag.TYPE_DEBITO : PlugPag.TYPE_PIX,
                    (int)(amount * 100),
                    PlugPag.INSTALLMENT_TYPE_A_VISTA,
                    1,
                    reference,
                );

                // Cria a identificação do aplicativo
                PlugPagAppIdentification appIdentification = new PlugPagAppIdentification("testepagseguro", "1.0.0+1");

                // Cria a referência do PlugPag
                PlugPag plugpag = new PlugPag(context, appIdentification);

                // Ativa terminal e faz o pagamento
                int initResult = plugpag.initializeAndActivatePinpad(new 
                PlugPagActivationData("403938"));

                if (initResult == PlugPag.RET_OK) {
                    PlugPagTransactionResult transResult = plugpag.doPayment(paymentData);
                    String status = transResult.getReturnMessage();
                    result.success(status);
                    // Trata o resultado da transação
                } else {
                    result.error("ERRO", "Falha ao inicializar o terminal", null);
                }
            }
        });
    } else {
        result.notImplemented();
    }
  }

}
*/