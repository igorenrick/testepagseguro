import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:testepagseguro/plugpag_payment.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int index = 0;

  late StreamSubscription _statusSubscription;
  late StreamSubscription _makingPaymentSubscription;

  @override
  void initState() {
    super.initState();
    _statusSubscription = PlugPagPayment.statusStream.listen((status) {
      setState(() {
        PlugPagPayment.status = status;
      });
    });
    _makingPaymentSubscription =
        PlugPagPayment.makingPaymentStream.listen((makingPayment) {
      setState(() {
        PlugPagPayment.makingPayment = makingPayment;
      });
    });
  }

  @override
  void dispose() {
    _statusSubscription.cancel();
    _makingPaymentSubscription.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        body: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  PlugPagPayment.status.contains(',')
                      ? PlugPagPayment.status.split(',')[1]
                      : PlugPagPayment.status,
                ),
                ElevatedButton(
                  child: const Text('Débito'),
                  onPressed: () async {
                    index++;
                    String type = 'debito';
                    double amount = 1.00 + index;
                    String reference = 'testReference${index.toString()}';

                    await PlugPagPayment.makePayment(
                      type,
                      amount,
                      reference,
                    );
                  },
                ),
                ElevatedButton(
                  child: const Text('Crédito'),
                  onPressed: () async {
                    index++;
                    String type = 'credito';
                    double amount = 8.00;
                    String reference = 'testReference${index.toString()}';

                    await PlugPagPayment.makePayment(
                      type,
                      amount,
                      reference,
                    );
                  },
                ),
                ElevatedButton(
                  child: const Text('Pix'),
                  onPressed: () async {
                    index++;
                    String type = 'pix';
                    double amount = 1.00 + index;
                    String reference = 'testReference${index.toString()}';

                    await PlugPagPayment.makePayment(
                      type,
                      amount,
                      reference,
                    );
                  },
                ),
                PlugPagPayment.makingPayment
                    ? ElevatedButton(
                        child: const Text('Abortar Pagamento'),
                        onPressed: () async {
                          await PlugPagPayment.abortPayment();
                        },
                      )
                    : const SizedBox(),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
