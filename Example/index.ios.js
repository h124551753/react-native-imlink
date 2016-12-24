/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  Button,
  View
} from 'react-native';

import ImLink from 'react-native-imlink';

export default class Example extends Component {
  // 构造
  constructor(props) {
    super(props);
    // 初始状态
    this.state = {
      apSsid: '',
      config: false,
      result: {},
      errMsg: ''
    };
  }

  componentDidMount () {
    ImLink.getSsid(this._ssidCallback);
  }

  _ssidCallback = (ssid) => {
    this.setState({
      apSsid: ssid
    })
  };

  _startConfig = () => {
    this.setState({
      config: true
    });

    ImLink.start({
      ssid: this.state.apSsid,
      password: '26554422',
      count: 2
    }).then((result) => {
      console.log(result);
      this._stopConfig()
    }).catch((error) => {
      console.log(error);
      this._stopConfig()
    })
  };

  _stopConfig = () => {
    ImLink.stop();
    this.setState({
      config: false
    });
  };

  _onPress = () => {
      if(this.state.config){
        this._stopConfig();
      } else {
        this._startConfig();
      }
  };

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          {'apSsid: ' + this.state.apSsid}
        </Text>

        <Button style={{marginTop: 30, padding: 10}}
                onPress={this._onPress }
                disabled={this.state.config}
                title={this.state.config ? '停止' : '开始'}/>
        <Text style={{marginTop:40}}>
          result:
        </Text>
        <Text style={{marginTop:40}}>
          {this.state.errMsg}
        </Text>
      </View>
    );
  }

  componentWillUnMount () {
    this._stopConfig();
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('Example', () => Example);
