# f1_driver_rankings

The project structure is shown as below,
![image](https://github.com/yugu-yg/f1_driver_rankings/assets/93423865/e3038acd-864f-464c-9e5b-f6210c79ff3e)

There's 3 main funtional part in this project:
1. Android App: This android app is used to collect user search string, create HTTP requests, and show the search results returned from the web api.
2. MongoDB Storage: A MongoDB database is created on MongoDB Atlas and ready to hold the data collected from 3rd party api.
3. Web API: This web api first collects f1 driver rankings in several years from Ergast Developer API (http://ergast.com/mrd/), then stores the data into remote MongoDB cluster. Then it will process HTTP Get requests and send back the results to the android user. It also has a JSP dashboard displaying the log of searches.
