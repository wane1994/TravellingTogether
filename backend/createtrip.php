<?php 
require "conn.php";
$type = $_POST["type"];
$username = $_POST["username"];
$from = $_POST["from"];
$to = $_POST["to"];
$day = $_POST["day"];
$month = $_POST["month"];
$year = $_POST["year"];
$hour = $_POST["hour"];
$minute = $_POST["minute"];


$mysql_qry = "insert into trips (type, username, fromtrip, totrip, day, month, year, hour, minute) values ('$type','$username','$from','$to','$day','$month','$year','$hour','$minute')";

if($conn->query($mysql_qry) === TRUE) {
echo "Your trip created successfully";
}
else {
echo "Error: " . $mysql_qry . "<br>" . $conn->error;
}
$conn->close();

?> 