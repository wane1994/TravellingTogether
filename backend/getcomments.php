<?php 
require "conn.php";
$tripid = $_POST["tripid"];
$sql = "select * from comments where tripid like '$tripid';";
$res = mysqli_query($conn,$sql);
 
$result = array();
 
while($row = mysqli_fetch_array($res)){
array_push($result,
array('username'=>$row[2],
'comment'=>$row[3]
));
}
 
echo json_encode(array("result"=>$result));
 
$conn->close();

?> 