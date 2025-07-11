import { lynx } from 'lynx';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    lynx.echo({ value: inputValue })
}
