#include <SoftwareSerial.h>

SoftwareSerial BTSerial(2,3);

int ECG_pin = 0;
int count = 0; 
int rvalue = 0;
int index = 0;
bool start = false;
int empty = 0;

void setup() {
  Serial.begin(9600);
  BTSerial.begin(9600);

}

int BTreadV= 0;
void loop(){
  rvalue = analogRead(0) / 4; //write가 최대 256까지 보낼 수 있기 때문
  if(BTSerial.available()==true){ //블루투스로 신호를 받으면
    //Serial.println(BTSerial.read());
    BTreadV = BTSerial.read();
    if(BTreadV==49){ // 그 신호가 1(49)인경우( 시작 버튼 누를 경우 )
      Serial.println("test");
      start = true;
      count = 500;
      index = 0; 
    }
		if(BTreadV==48){
      Serial.println("test2");
      start = false;
    }
  }

  if(start==true){
    
		Serial.println(rvalue);
    BTSerial.write(rvalue);
  }

  if(start == false){
    empty = 500-count;
  }

  delay(100); //0.1초 간격으로 측정

}