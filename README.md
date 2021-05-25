# RaspiOpener

Open source project to remotely operate electronic door openers.
You need to connect a raspberry pi (zero?) with a relay to the normal button that you use.
The App to use with it is "DoorOpenerApp"

# Installation Guide:
 Install "RaspiOpener"  "sudo java -jar AliveKeeper.jar" in new screen

- flash sd card with RaspberryOS Lite (https://www.raspberrypi.org/software/operating-systems/)
- copy your wpa-supplicant.conf and the ssh file on the boot partition
  Thats how the wpa-supplicant.conf file could look:
  ```sh
  country=[insert your country code here without the square brackets]
  update_config=1
  ctrl_interface=/var/run/wpa_supplicant

  network={
    ssid="MyTestNetwork"
    psk="Password"
  }
  ```
- install all required packages:
  ```sh
  sudo apt-get install openjdk-8-jdk -y & sudo apt-get install wiringpi -y & sudo apt-get install screen -y & sudo apt-get install git -y
  ```
- download the installer
  ```sh
  wget https://github.com/Kreck-Projekt/PiInstaller/releases/download/V1.0/installer.sh
  ```   

- making the installer executable
  ```sh
  chmod +x installer.sh
  ```
- run the installer  
  ```sh
  ./installer.sh
  ```
- create a new screen and execute the AliveKeeper.jar
  ```sh
  screen -S RaspiOpener
  sudo java -jar AliveKeeper.jar
  ```
  To exit hit Ctrl + A and then d

- <p>now install the app and go through the init process in the app.(only android) <br>Download Link: https://github.com/Kreck-Projekt/DoorOpenerApp/releases/download/v1.0/door-opener-release.apk </p>
- If the initializing was succesful you should now open port 5000 in your network.
- After that change the IP-Addres in the app
  You can look up your public ip here:
  https://whatismyipaddress.com
  
  !Warning! After the reboot you must redo the last step 

# Developer Notes:
### Command syntax:

Store Key: k:'key' <br/>
Store Password: p:('hash');'nonce' <br/>
Change Password: c:('oldHash';'newHash');'nonce' <br/>
Set new OTP: "s:('otp';'hash');'nonce' <br/>
Use OTP: e:'otp';'time' <br/>
Open: o:('hash';'time');'nonce' <br/>
Reset: r:('hash');'nonce' <br/>
