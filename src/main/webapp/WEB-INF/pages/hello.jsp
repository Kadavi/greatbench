<html>
<head>
    <title>Striping</title>
    <style>
        html,
        body {
            margin:0;
            padding:0;
            height:100%;
            background: url("/resources/images/parkbg.jpg") no-repeat center center fixed;
            -webkit-background-size: 75% 100%;
            -moz-background-size: 75% 100%;
            -o-background-size: 75% 100%;
            background-size: 75% 100%;
            background-color: darkcyan;
        }
        #container {
            width: 75%;
            min-height: 100%;
            position: relative;
            margin: 0 auto;
        }
        #header {
            background: #ff0;
            padding: 20px;
        }
        #body {
            padding:10px;
        }
        #footer {
            position:absolute;
            bottom:0;
            width:100%;
            height:20px;   /* Height of the footer */
            background:#6cf;
            text-align: center;
        }
    </style>
</head>
<body>
<!--
	<h1>${message}</h1>
    <form action="" method="POST">
        <script
                src="https://checkout.stripe.com/checkout.js" class="stripe-button"
                data-key="pk_test_3pykNl2uOMSk8iluOnXIpLCV"
                data-amount="2000"
                data-name="Demo Site"
                data-description="2 widgets ($20.00)"
                data-image="/resources/images/parkbg.jpg">
        </script>
    </form>
    <div style="height: 100px; background-color: red; bottom: 5px; position: absolute;"></div>
-->

    <div id="container">
        <div id="header"></div>
        <div id="body"></div>
        <div id="footer">jlkj</div>
    </div>

</body>
</html>