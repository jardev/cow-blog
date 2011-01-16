(ns blog.config
  (:import java.io.FileNotFoundException)
  (:import java.io.File)
  (:require (net.briancarper [postgres-pool :as pg])))


(def DEBUG true)

(def SITE-TITLE "Yaroslav Luzin himself")
(def SITE-DESCRIPTION "Clojure. Software development. Project management")
(def SITE-URL "http://127.0.0.1:8001")

(def PUBLIC-DIR "public")  ;;CS/JS/images live here.  Relative path.
(def TIME-ZONE "Europe/Kiev")
(def TIME-FORMAT "MMMM dd, yyyy @ h:mm a z")

;; Change this.
(def PASSWORD-SALT "K@#$J@$#(FJ@#!$M@#n2NELKDwdjf9baf1_3krJ@!FKnjef7i#JR@R")

(def DEFAULT-COMMENT-AUTHOR "Anonymous")

(def POSTS-PER-PAGE 13)
(defn page-offset [page-number]
  (* (dec page-number) POSTS-PER-PAGE))

(def TAG-CATEGORY-TITLE-REGEX #"^[-A-Za-z0-9_. ]+$")
(def TAG-CATEGORY-URL-REGEX #"^[a-z0-9_-]+$")

(def CAPTCHA #"(?i)^\s*moo\s*$")

(def DB (pg/postgres-pool {:database "blog"
                           :username "blog"
                           :password "qwertyuiop"}))
(def HTTP-PORT 8001)
(def HTTP-HOST "0.0.0.0")

;; Specify your Google Analytics Account
(def GA-ACCOUNT nil)

(def RETWEET-ACCOUNT "tweetmeme")

;; Load config_local.clj
(defn join-path [& pathes]
  (reduce (fn [res path] (.toString (File. res path))) pathes))

(try
  (load-file "config_local.clj")
  (catch FileNotFoundException e
    (try
      (load-file (join-path (File. *file*) "../../config_local.clj"))
      (catch FileNotFoundException e
        (println "Create config_local.clj")))))
