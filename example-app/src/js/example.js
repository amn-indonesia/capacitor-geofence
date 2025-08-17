import { BackgroundGeolocation } from 'capacitor-background-geolocation';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    BackgroundGeolocation.echo({ value: inputValue })
}
