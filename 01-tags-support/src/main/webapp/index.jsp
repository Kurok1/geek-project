<%@ taglib prefix="ex" uri="/WEB-INF/customResponseHeader.tld"%>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Document</title>
</head>
<body>
    <ex:ResponseHeaders header="Cache-Control" value="true" />
    <ex:ResponseHeaders header="X-REQUEST-ID" value="123456" />
</body>
</html>