This OTP API module takes in 2 arguments.

Argument:

1- gen

take in an input: userid

e.g. gen?userid={{userid}} ,replace {{xxxx}} with value

2- validate

take in 2 input: userid & token

e.g. validate?userid={{userid}}&token={{token}}

Will generate a base32 secretkey and save into a postgreSQL

Take in a base32 secretkey,for algorithm
