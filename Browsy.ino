/*
This project requires a Rfduino and led + buzzer connected to the pin 2 and 3. The goal is to control these devices from the
web app to give native app free future to the home automation industry.
*/


#include <RFduinoBLE.h>

byte value = 0;

int led = 2;
int buzzer = 3;

  
void setup() {

  pinMode(led, OUTPUT);
  pinMode(buzzer, OUTPUT);  
   
  RFduinoBLE.deviceName = "Your_Device_Name";   
  RFduinoBLE.advertisementData = "Your_Adv_Data";  
  RFduinoBLE.begin();
  
}


void loop() {

   if(value != 0)
  {
     if(value == 1) // start the led
    {     
       digitalWrite(led, HIGH);
       delay(3000); 
       value = 0;  
       digitalWrite(led, LOW);    
    }
     else if(value == 2) // start the led
     {     
       digitalWrite(buzzer, HIGH);
       delay(3000); 
       value = 0;  
       digitalWrite(led, LOW);    
     }
      
  }
  
}

  void RFduinoBLE_onReceive(char *data, int len)
{
    if(data[0] == '1')
  {
     value = 1;
     
  }else if(data[0] == '2')
  {
     value = 2;
  }
     
}
