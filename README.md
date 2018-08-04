# DAaDA (Data Anonymization & De-Anoymization)

DAaDA is a toolkit to do data anoymization and de-anoymization on your PII fields. The main use cases of DAaDA is listed below,
  - To maintain complete security on your customer's PII information
  - You want to maintain the security but needs to do some analyis (Analytical work to build their recommendation engine / to run machine learning jobs /...) based on your cusotmer's PII data.
  - You want to share your customer's data across borders and wants to adhere to your country's policies like EU GDPR / US Pricvacy act / ...  

DAaDA solves all the above problems with the single tool kit without any issues.

Its a developer friendly library and has below features for them,

  - Its a simple library so that you can place it and use it in your existing applications without any other third party libs (Because its fat jar).
  - Migrate your existing data set with anonymized set with minimal changes (Mostly memory because of encrypted data size, its also based on your selected key size).
  - You can add any number of PII field implementations and hook it to the tool-kit without any issues.
  - You can also override existing PII field implementations based on your need.
  - By default it comes with commandline options to migrate your given CSV files to anonymized one. It runs on parallel threads so faster.
  
# Default PII implementations

  - IMEI
  - IMSI
  - Name (First name / Last name / Nick name / Sur name / ...)
  - Email (Social (media) ids)
  - Data of birth
  - IP address
  - IMEI

