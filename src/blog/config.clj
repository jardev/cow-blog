(ns blog.config
  (:require (net.briancarper [postgres-pool :as pg])))

(def DEBUG false)

(def SITE-TITLE "Yaroslav Luzin himself")
(def SITE-TITLE-ADDITIONAL "aka @jardev")
(def SITE-DESCRIPTION "Clojure. Software development. Project management")
(def SITE-URL "http://localhost:8001")

(def PUBLIC-DIR "public")  ;;CS/JS/images live here.  Relative path.
(def TIME-ZONE "Canada/Pacific")
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

;; Pick a DB...
(comment
  ;;Postgres pool, uses net.briancarper.postgres-pool from Clojars
  (def DB (pg/postgres-pool {:database "blogtest"
                             :username "blogtest"
                             :password "blogtest"}))

  ;; Normal single-connection postgres
  (def DB {:classname "org.postgresql.Driver"
           :subprotocol "postgresql"
           :subname "//localhost/blogtest"
           :username "blogtest"
           :password "blogtest"})

  ;; MySQL
  (def DB {:classname "com.mysql.jdbc.Driver"
           :subprotocol "mysql"
           :subname "//localhost/blog?user=blogtest&password=blogtest"}))
