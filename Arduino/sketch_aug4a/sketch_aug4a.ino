#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11);  // RX, TX
char inputByte = 'z';
uint8_t LEFT = 5;
uint8_t RIGHT = 6;

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);  // There's a LED that turn on if there are input data
  pinMode(LEFT, OUTPUT);//left
  pinMode(RIGHT, OUTPUT);//right
  Serial.begin(9600);
  mySerial.begin(9600);
}

void loop() {  // run over and over

  while (mySerial.available()) {
    inputByte = mySerial.read();
    Serial.println(inputByte);
    if (inputByte == 'L') {
      leftTurn();
      digitalWrite(LED_BUILTIN, HIGH);
    } else if (inputByte == 'R') {
      rightTurn();
      digitalWrite(LED_BUILTIN, HIGH);
    }else if(inputByte == 'N'){
      stopTurn();
      digitalWrite(LED_BUILTIN, LOW);
    }
    delay(100);
  }
}

void leftTurn(){  
  digitalWrite(RIGHT,LOW);  
  digitalWrite(LEFT,HIGH);
}

void rightTurn(){
  digitalWrite(LEFT,LOW);
  digitalWrite(RIGHT,HIGH);
}
void stopTurn(){
  digitalWrite(LEFT,LOW);
  digitalWrite(RIGHT,LOW);
}