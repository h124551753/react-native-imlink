# react-native-imlink

react native imlink wifi for android and ios

##install
1. npm install react-native-imlink --save
2. react-native link react-native-imlink

## Usage

``` javascript
//import module
import ImLink from 'react-native-imlink';

//get current connected wifi ssid
//return result by callback function
ImLink.getSsid((ssid) => {
	console.log(ssid);
});

//start imlink
ImLink.start({
  ssid: 'apssid',       //string: ssid
  password: '123456',   //string: password
  count: 1              // int: config devices count
}).then((result) => {
  console.log(result);
}).catch((error) => {
  console.log(error);

})

//interrupt imlink
ImLink.stop();

```
