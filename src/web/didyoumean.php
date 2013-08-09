<?php
$query = $_GET["query"];

ini_set('memory_limit','1024M');

$filename = "big.txt";
 
/* get content of $filename in $content */
$content = strtolower(file_get_contents($filename));
 
/* split $content into array of substrings of $content i.e wordwise */
$wordArray = preg_split('/[^a-z]/', $content, -1, PREG_SPLIT_NO_EMPTY);
 
/* "stop words", filter them */
/*$filteredArray = array_filter($wordArray, function($x){
       return 		!preg_match("/^(.|a|an|and|the|this|at|in|or|of|is|for|to)$/",$x);
     });*/
 
/* get associative array of values from $filteredArray as keys and their frequency count as value */

$wordFrequencyArray = array_count_values($wordArray);

$splitQuery = split(" ",$query);
$rst = "";
foreach($splitQuery as $word)
	$rst = $rst.correct($word,$wordFrequencyArray);

echo trim($rst);

function edit1($word){
	$list1 = delete($word);
	$list2 = insert($word);
	$list3 = transpose($word);
	$list4 = replace($word);
	$union = array_merge($list1,$list2,$list3,$list4);
	$union = array_unique($union);
	return $union;
}

function delete($word){
	$list = array();
	$str = str_split($word);
	for($i=0;$i<count($str);$i++){
		$temp = $str[$i];
		$str[$i] = "";
		$rst = implode($str);
		array_push($list,$rst);
		$str[$i] = $temp;
	}
	/*foreach($list as $item){
		echo $item."\n";	
	}*/
	return $list;
}

function insert($word){
	$list = array();
	$str = str_split($word);
	$alphabet = array('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z');
	foreach($alphabet as $letter){
		$str1 = $str;
		array_push($str1,$letter);
		$rst = implode($str1);
		array_push($list,$rst);
		for($i=1;$i<count($str1);$i++){
			$temp = $str1[count($str1)-1-$i];
			$str1[count($str1)-1-$i] = $str1[count($str1)-$i];
			$str1[count($str1)-$i] =$temp;
			$rst = implode($str1);
			array_push($list,$rst);
		}
	}
	/*foreach($list as $item){
		echo $item."\n";	
	}*/
	return $list;
}

function replace($word){
	$list = array();
	$str = str_split($word);
	$alphabet = array('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z');
	foreach($alphabet as $letter){

		for($i=0;$i<count($str);$i++){
			$str1 = $str;
			$str1[$i] = $letter;
			$rst = implode($str1);
			if(strcmp($rst,$word)!=0) array_push($list,$rst);		
		}	
	}
	/*foreach($list as $item){
		echo $item."\n";	
	}*/
	return $list;
}

function transpose($word){
	$list = array();
	$str = str_split($word);
	for($i=0; $i<count($str)-1;$i++){
		$str1=$str;
		$temp = $str1[$i];
		$str1[$i] = $str1[$i+1]; 
		$str1[$i+1] = $temp;
		$rst = implode($str1);
		array_push($list,$rst);
	}
	/*foreach($list as $item){
		echo $item."\n";	
	}*/
	return $list;
}

function correct($word,$wordFrequencyArray){
	$correct_term = $word;
	if(strlen($word)<2) return;
	if(array_key_exists($word,$wordFrequencyArray)){
		return "$word ";		
	}
	$editted_list = edit1($word);
	$max = 0;
	foreach($editted_list as $item){
		
		if(array_key_exists($item, $wordFrequencyArray)){
			if($wordFrequencyArray[$item] > $max){
				$max = $wordFrequencyArray[$item];
				$correct_term = $item;
			}
		} 	
	}
	return "$correct_term ";
}


?>
