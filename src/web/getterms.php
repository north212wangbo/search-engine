<?php

$file_name = "terms.txt";

$file = fopen($file_name, "r");

$terms = split("\n", file_get_contents($file_name));

echo json_encode($terms);
?>
