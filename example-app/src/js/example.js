import { Geofence } from 'capacitor-geofence';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    Geofence.echo({ value: inputValue })
}
