<?php 
require "conn.php";
$username = $_POST["username"];
$password = $_POST["password"];
$name = $_POST["name"];
$surname = $_POST["surname"];
$phonenumber = $_POST["phonenumber"];


$mysql_qry = "insert into users (username, password, name, surname, phonenumber) values ('$username','$password','$name','$surname','$phonenumber')";

if($conn->query($mysql_qry) === TRUE) {
echo "Registration successful";
}
else {
echo "Error: " . $mysql_qry . "<br>" . $conn->error;
}
$conn->close();

?> 