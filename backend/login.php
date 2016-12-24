<?php 
require "conn.php";
$username = $_POST["username"];
$userpass = $_POST["password"];
$sql = "select * from users where username like '$username' and password like '$userpass';";
$result = mysqli_query($conn, $sql);

if(mysqli_num_rows($result)>0) {
echo "Login success";
}
else {
echo "Login error, please try again";
}

?> 