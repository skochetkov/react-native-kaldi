import React, { useState , Component} from "react";
import { StyleSheet, Button, Text, View, AppRegistry } from "react-native";
import { NativeEventEmitter, NativeModules } from 'react-native';
import KaldiExample from './KaldiExample';


export default class KaldiNative extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      errorstate: '',
      uistate: '',
      collected: '', 
      isRecognized: false,
      isInitialized: true,
      isStopped: false,
      transcripted: '',
      results: [],
    }
 
    //const [someVar, setSomeVar] = useState('');  
    //const [uistate, setUiState] = useState("Here will be displayed events from Kaldi");
    //const [errorstate, setErrorState] = useState("Here will be displayed error events from Kaldi");
    
  }

  /*componentWillUnmount() {
    //Voice.destroy().then(Voice.removeAllListeners);
    KaldiExample.destroy();
  }*/

  componentDidMount() {
    const eventEmitter = new NativeEventEmitter(NativeModules.KaldiExample);
    
    this.eventListener = eventEmitter.addListener('uistate', (event) => {
      this.setState({uistate: event.eventProperty});

      if(!event.eventProperty) return;
      const json = event.eventProperty;

      var n = json.indexOf(":");
      if(n <= 0) return;

      const name = json.slice(0, n).replace(/\W/g, '')
      const value = json.slice(n + 1);

      //console.log(value);

      if(name == 'result') {

        JSON.parse(value, (key, val) => {

          if (val && typeof val === "string" && key == 'text') {
            //console.log('key:' + key +' value:'+ val);
            this.state.transcripted += (val + '\n');
          }
        });
      }
      
    });

    this.eventListener = eventEmitter.addListener('errorstate', (event) => {
      this.setState({errorstate: event.eventProperty});
      
    });
  }

  componentWillUnmount() {
    this.eventListener.remove(); //Removes the listener
    KaldiExample.destroy();
  }
  
  render() {
    return (
      <View>
        <Text>
          Error Events: {this.state.errorstate}
        </Text>
        <Button
          onPress={ async () => {
            this.state.isInitialized = false;
            this.state.isRecognized = true;
            try {
              await KaldiExample.init(1);
            } catch (err) {
              console.warn(err);
            }
          }}
          disabled={!this.state.isInitialized}
          title={"Init Kaldi"}
        />
        <Button
          onPress={ async () => {
            try {
              this.state.isRecognized = false;
              this.state.isStopped = true;
              console.group("running recognize mic");
              await KaldiExample.recognizeMicrophone();
            } catch (err) {
              console.warn(err);
            }
          }}
          disabled={!this.state.isRecognized}
          title={"Recognize Microphone"}
        />
        <Button
          onPress={ async () => {
            try {
              this.state.isRecognized = true;
              this.state.isStopped = false;
              console.group("stopping recognize mic");
              await KaldiExample.stop();
            } catch (err) {
              console.warn(err);
            }
          }}
          disabled={!this.state.isStopped}
          title={"Stop"}
        />
        <Text>
          Events: {this.state.uistate}
        </Text>
        <Text style={styles.transcript}>
            Transcript
        </Text>
        <Text style={styles.transcript}> {this.state.transcripted}</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  transcript: {
    textAlign: 'center',
    color: '#B0171F',
    marginBottom: 1,
    //top: '400%',
  },
});

AppRegistry.registerComponent('KaldiNative', () => KaldiNative);



