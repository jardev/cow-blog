(defproject cow-blog "0.2.0-jardev"
  :description "Brian Carper's cow-blog fork by @jardev"
  :url "http://github.com/jardev/cow-blog"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [gaka "0.1.0"]
                 [compojure "0.4.0"]
                 [hiccup "0.2.4"]
                 [clout "0.2.0"]
                 [ring/ring-jetty-adapter "0.2.0"]
                 [ring/ring-devel "0.2.0"]
                 [oyako "0.1.1"]
                 [postgres-pool "1.1.0"]
                 [joda-time "1.6"]
                 [rhino/js "1.7R2"]
                 [sandbar/sandbar "0.2.3"]]
  :dev-dependencies [[swank-clojure "1.2.1"]])
