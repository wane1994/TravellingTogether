<?php 
require "conn.php";
$username = $_POST["username"];
$userpass = $_POST["password"];
$sql = "select * from users where username like '$username' and password like '$userpass';";
$res = mysqli_query($conn,$sql);
 
$result = array();
 
while($row = mysqli_fetch_array($res)){
array_push($result,
array('username'=>$row[0],
'name'=>$row[2],
'surname'=>$row[3],
'phonenumber'=>$row[4]
));
}
 
echo json_encode(array("result"=>$result));
 
$conn->close();

?> 