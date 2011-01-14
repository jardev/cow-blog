(ns blog.config
  (:import java.io.FileNotFoundException)
  (:require (net.briancarper [postgres-pool :as pg])))

(def DEBUG false)

(def SITE-TITLE "Yaroslav Luzin himself")
(def SITE-TITLE-ADDITIONAL "aka @jardev")
(def SITE-DESCRIPTION "Clojure. Software development. Project management")
(def SITE-URL "http://localhost:8001")

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

(def DB nil)

(def DB (pg/postgres-pool {:database "blog"
                           :username "blog"
                           :password "qwertyuiop"}))
(def HTTP-PORT 8000)

(try
  (load "config_local.clj")
  (catch FileNotFoundException e
    (println "Create config_local.clj")))