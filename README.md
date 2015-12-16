# NoSql Plugin for IntelliJ IDEA version 0.1.0-SNAPSHOT

## Description
This plugin is a fork from [mongo4idea](https://github.com/dboissier/mongo4idea) and intends to integrate Redis and Couchbase databases. Please note that the Couchbase integration is experimental because I am not a strong user of this database.

## Current status: EAP

[Download the current SNAPSHOT](https://github.com/dboissier/nosql4idea/raw/master/snapshot/nosql4idea-0.1.0-SNAPSHOT-distribution.zip).

## Plugin Compatibility

This plugin is built with JDK 1.7 and idea 14.1 version.

The plugin has been tested with the following databases:
* MongoDB 2.7 and 3.0
* Redis 2.8.21
* Couchbase Community 4.0.0

## Installation 
 
To install it : `Settings > Plugins > Install plugin from Disk`

## Configuration

On the right, you will see a new tool window named NoSql Explorer

![NoSqlBrowser](https://github.com/dboissier/nosql4idea/raw/master/doc/explorer.png)

* Click on the Wrench Icon from the toolbar and you will be redirected to the Plugin Settings.
* You can specify the mongo and redis CLI paths at the top of the panel
* To add a server, click on the **\[+\]** button and choose you database vendor

![SettingsAddAServer](https://github.com/dboissier/nosql4idea/raw/master/doc/settings_add_a_server.png)

* Click on the **OK** button and enter the settings of your database

![MongoSettings](https://github.com/dboissier/nosql4idea/raw/master/doc/mongo_settings.png)

* When all your dabatase are configured you should see then in the explorer panel
 
![NoSqlBrowserWithServers](https://github.com/dboissier/nosql4idea/raw/master/doc/explorer_with_servers.png)

## Viewing the Redis database content

Double click on the database icon from your redis server and the results will appear as a tab

![RedisResults](https://github.com/dboissier/nosql4idea/raw/master/doc/redis_results.png)

You can filter the results (Currently, it runs a `KEYS <filter>` command. A `SCAN <filter>` will replace it in the future for optimization purpose.)

Like the **Properties editor**, you can group your data by prefix. Click on the corresponding icon and then click on the Elipsis icon to set you separator

![RedisResultsGroupedByPrefix](https://github.com/dboissier/nosql4idea/raw/master/doc/redis_group_by_prefix.png)

## Viewing the Couchbase database content
 
Double click on the database icon from your couchbase server and the results will appear as a tab

![CouchbaseResults](https://github.com/dboissier/nosql4idea/raw/master/doc/couchbase_results.png)

**Important note**
To get the results from each bucket, an **Index** must be created. Otherwise an error message is raised.

## Roadmap

### 0.1.0

* integrate Redis : view results with 'Group by prefix' feature like **properties editor**
* integrate Couchbase : view results 

### 0.2.0

* delete, update and add features for Redis and Couchbase


