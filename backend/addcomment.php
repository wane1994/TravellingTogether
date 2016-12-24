<?php 
require "conn.php";
$tripid = $_POST["tripid"];
$username = $_POST["username"];
$comment = $_POST["comment"];


$mysql_qry = "insert into comments (tripid, username, comment) values ('$tripid','$username','$comment')";

if($conn->query($mysql_qry) === TRUE) {
echo "Comment added";
}
else {
echo "Error: " . $mysql_qry . "<br>" . $conn->error;
}
$conn->close();

?> 