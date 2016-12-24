<?php 
require "conn.php";
$type = $_POST["type"];
$sql = "select * from trips where type not like '$type';";
$res = mysqli_query($conn,$sql);
 
$result = array();
 
while($row = mysqli_fetch_array($res)){
array_push($result,
array('tripid'=>$row[0],
'username'=>$row[2],
'from'=>$row[3],
'to'=>$row[4],
'day'=>$row[5],
'month'=>$row[6],
'year'=>$row[7],
'hour'=>$row[8],
'minute'=>$row[9]
));
}
 
echo json_encode(array("result"=>$result));
 
$conn->close();

?> 