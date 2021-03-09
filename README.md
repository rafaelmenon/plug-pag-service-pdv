# plug-pag-service-pdv
## React Native Plug Pag Service PDV365

Esta biblioteca destina-se a integradores que utilizarão os terminais da linha Smart do
PagSeguro como solução de pagamento integrada através do serviço PlugPagService.


## "react-native": ">=0.63.4 <0.59.0"

## Instalação

plug-pag-service-pdv requer [Node.js](https://nodejs.org/) ou [Yarn](https://yarnpkg.com).

Instale a dependências.

```sh
npm install plug-pag-service-pdv ou yarn add npm plug-pag-service-pdv
```

## Importação

```javascript
import PlugPagService from 'plug-pag-service-pdv';
```

## Exemplos

Número de série:

```javascript
function handleGetSerialNumber() {
    PlugPagService.getSerialNumber()
      .then((initResult) => {
        console.log('success', initResult);
      }, error => {
        console.error('error', error.message);
      });
  }
```

Identificação de aplicativo:

```javascript
 function handleSetAppIdendification() {
    PlugPagService.setAppIdendification("RNPAGA", "1.0")
  }
```

Evento Listener:

```javascript
 const calendarManagerEmitter = new NativeEventEmitter(PlugPagService);
 const subscription = calendarManagerEmitter.addListener(
    'eventPayments',
    (reminder) => console.log(reminder)
  );
```

Inicializar e ativar o Pin pad:

```javascript
 function handleInitializeAndActivatePinpad() {
    handleSetAppIdendification()
    PlugPagService.initializeAndActivatePinpad("codigo_ativação").then((initResult) => {
      if (initResult.retCode === PlugPagService.RET_OK) {
        // Define os dados do pagamento
        const paymentData = {
          amount: 19 * 100, //VALOR
          installmentType: 1, //A VISTA OU PARCELADO
          installments: 1, //PARCELAS
          type: 1, //TIPO DEBITO OU CREDITO OU VOUCHER
          userReference: 'PAGAMENTO', //REFERENCIA
          printReceipt: false //IMPRIMIR RECIBO
        };
        PlugPagService.doPayment(JSON.stringify(paymentData)).then((initResult) => {
          console.log("SUCESSO", initResult);
        }, error => {
          console.error('ERRO', error);
        });
      }
    }, error => {
      console.error('ERROR', error);
    });

  }
```
