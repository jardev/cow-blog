# Overview

This a fork of cow-blog - a blog engine written by Brian Carper.

## cow-blog 0.2.0
by [Brian Carper](http://briancarper.net/)

This is a(nother) complete rewrite of my blog engine, written in Clojure using Compojure and PostgreSQL.

Version 0.1.0 of this code used Tokyo Cabinet.  I ran it that way successfully for a year in hobby-production, but I don't advise you to do the same.  There were issues with thread-safety and stability and it was a huge hack / proof of concept.  Use it at your own risk.

Version 0.2.0 currently depends upon a highly experimental ORM-ish library called [Oyako](http://github.com/briancarper/oyako) which I'm building at the same time I build this blog engine.

## Purpose

This code runs my hobby-website.  Its purpose is to teach me how to write webapps in Clojure, and to have fun while doing so.

The intended audience for this code is a knowledgeable Clojure programmer.  User-friendliness is almost entirely lacking.  Users posting comments might get semi-helpful error messages when (not if) something breaks, but as an admin, you'll get stacktraces.

Get the picture?  I wouldn't use version even 0.2.0 of this code for anything you make money from.  But it might be fun to play with.

## Features

* Post tags, categories, comments
* Archives, with gratuitous tag cloud
* Markdown
* Gravatars
* RSS
* Lame spam filter
* Add/edit/delete posts/tags/categories via admin interface
* SyntaxHighlighter

# Getting started

Clone this git repo, then cd into the directory and:

    lein deps

Create `config_local.clj` and add settings to your tastes. This file will overwrite `src/blog/config.clj` settings.

Look at (and run) `blog.db.postgres.clj/init-db-postgres` to create the tables in your database.

Use `blog.db/create-user` to create an admin user, or you'll never be able to do anything.

To create a database and an admin user:

    $ lein repl
    user=> (use 'blog/db)
    user=> (use '[blog.db.postgres :only [init-db-postgres]])
    user=> (init-db-postgres)
    user=> (with-db (create-user "user" "password"))

Once your tables are set up, then do this:

    make start

This will start the server. If you set DEBUG to true in your configuration file swank will start at the same time. So you can connect with slime and start hacking.

To stop the server run:

    make stop


# Deploying

By default deployment scripts are configurable. This will be fixed in the near future. So you have to use path settings from deploy.sh.

Create folders on your server:

    $ mkdir /opt/blog
    $ mkdir /opt/blog/tmp
    $ ln -s /opt/blog/tmp /opt/blog/active
    $ vi /opt/blog/active/config_local.clj

Set settings in config_local.clj such as HTTP-PORT, database connection settings. Then run from your local machine:

    $ DEPLOYTO=your.host.com make deploy

Set up your main web-server (e.g. nginx) to proxy to 127.0.0.1:8000, where 8000 is the port set in configuration files.


## Bugs

Bugs are a certainty.

For bug reports, feedback, or suggestions, please open an issue on github.

## LICENSE

See the `LICENSE` file.

## Changelog

* June 20, 2010 - Rewrite again?  No more Tokyo Cabinet.  Now uses Postgres.  Cows still missing.

* October 22, 2009 - Rewrite from scratch.  No more CRUD.  Tokyo Cabinet.  Removed cows.

* April 12, 2009 - Updated to work on Compojure's bleeding-edge "ring" branch.  Complete rewrite of the CRUD library.  Overhauled mostly everything.

* March 27, 2009 - Initial release.
