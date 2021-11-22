# Plugin to Pick Latest File/Folder in SFTP

This action plugin extracts the latest ingested folder/file in the SFTP directory to pass it to the pipeline. 

## Usage Notes
Common use case is to have the files uploaded on the SFTP server periodically. This plugin will output the latest arrived file or folder name. 
It can then be passed to FTP source plugin to pick just the latest files. 

This plugin targets use case where files are ingested to SFTP server periodically and 
batch pipeline should pick just the last arrived file/folder to append to target table (typical for a delta load pattern).
This picks the latest file arrived, this plugin can be used to build a datalake 
with files partitioned by ingestion dates.

## Plugin Configuration
| Configuration | Required      | Description                               |
|:------------- |:------------- |:------------------------------------------|
| Host Name     | Y             | Specifies the host name of the SFTP server|
| Port Number   | Y             | Specifies the port on which SFTP server is running|
| User Name	    | Y             | Specifies the name of the user to be used while logging to SFTP server|
| Password      | Y             | Password to log into SFTP Server|
| Directory     | Y             | Specifies the ingestion directory on the SFTP server where raw files are staged periodically|