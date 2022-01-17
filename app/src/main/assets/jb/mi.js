/**
 * These functions are only meant for calling back to the android app.
 * In no way are they meant to inhibit the performance of the jailbreak
 * as the calls will be asynchronous. AKA Helper functions to let the android
 * app know what is going on.
 */

function sendPrq(path, data) {
     var xhr = new XMLHttpRequest();
     xhr.open('POST', path, true);
     xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
     var data = JSON.stringify(data);
     xhr.send(data);
}

function sendHrq(path) {
     var xhr = new XMLHttpRequest();
     xhr.open('HEAD', path, true);
     xhr.send('');
}

function sendGrq(path) {
     var xhr = new XMLHttpRequest();
     xhr.open('GET', path, true);
     xhr.send('');
}

function response(message, data) {
    return {"response": message, "data": data }
}


function predone() {
    scanPayload()
}

function olddone()
{
    if(main_ret == 0 || main_ret == 179)
    {
        alert("You're all set!");
        setTimeout(function() { read_ptr_at(0); }, 1);
    }
    else
        alert("Jailbreak failed! Reboot your PS4 and try again.");
}


function done()
{
    if(main_ret == 0 || main_ret == 179)
    {
        sendMiSuccess()
        setTimeout(function() { read_ptr_at(0); }, 1);
    }
    else sendMiFailed()
}

function sendCommand(message, cmd){
    var path = "/jb/cmd"
    sendPrq(path, response(message, {"cmd": cmd}))
}

function scanPayload() {
     var message = "1337 things are happening right now, eta wen will this finished? eta s0n."
     var cmd = "send.payload"
     sendCommand(message, cmd)
}

function sendMiSuccess(){
     var message = "You're all set! Special thanks to the special players in the JB Community!"
     var cmd = "jb.success"
     sendCommand(message, cmd)
     alert(message)
}

function sendMiFailed(){
    var message = "Jailbreak failed. Please reboot your PS4 and try again"
    var cmd = "jb.failed"
    sendCommand(message, cmd)
    alert(message)
}
