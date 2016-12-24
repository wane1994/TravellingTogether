<?php 
require "conn.php";
$username = $_POST["username"];
$oldpassword = $_POST["oldpassword"];
$newpassword = $_POST["newpassword"];
$name = $_POST["name"];
$surname = $_POST["surname"];
$phonenumber = $_POST["phonenumber"];

$mysql_qry = "update users set name='$name', surname='$surname', phonenumber ='$phonenumber', password = '$newpassword' where username='$username' and password='$oldpassword' "; 

$query = $conn->query($mysql_qry);

if($conn->affected_rows > 0){
echo "Your data updated";
}
else { 
echo "Error: user data was not changed";
} 
$conn->close(); 

?>