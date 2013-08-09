<?php
$message = $_GET["message"];

//$fp = fsockopen("54.235.244.220", 40002, $errno, $errstr, 30);
$fp = fsockopen("localhost", 40000, $errno, $errstr, 30);

if (!$fp) {
    echo "$errstr ($errno)<br />\n";
} else {
    //send message
    $out = $message."\n";
    fwrite($fp, $out);
    //wait for result from master
    while (!feof($fp)) {
        echo fgets($fp);
    }
    fclose($fp);
}
?>
