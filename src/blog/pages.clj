(ns blog.pages
  "Functions in this namespace correspond to (public) pages.
  This namespace is a bridge between the DB and HTML-rendering code"
  (:require (blog [layout :as layout]
                  [link :as link]
                  [db :as db]
                  [time :as time]
                  [config :as config]
                  [util :as util]
                  [flash :as flash]
                  [error :as error]
                  [html :as html])
            (oyako [core :as oyako]
                   [query :as q])
            [ring.util [response :as response]]
            (clojure [pprint :as pprint])
            (clojure.contrib [string :as s])
            (hiccup [core :as hiccup])))

(defn index-page
  "Main index page."
  [& {:keys [user page-number] :or {page-number 1}}]
  (let [posts (oyako/fetch-all db/posts
                               :limit config/POSTS-PER-PAGE
                               :offset (config/page-offset page-number)
                               :admin? user
                               :post-type "blog")
        c (oyako/fetch-one (db/count-rows :posts) :admin? user :post-type "blog")]
    (if (seq posts)
      {:body (html/render-index posts
                                :count (:count c)
                                :user user
                                :page-number page-number)}
      (error/error 404 "There's nothing here."
                   "No posts have been written yet.  Blog owner: start writing!"))))

(defn post-page
  "Single-post page."
  [id & {:keys [user]}]
  (let [post (oyako/fetch-one db/posts
                              :id id
                              :admin? user
                              :include (q/query-> db/comments
                                                  :admin? user))]
    (if post
      {:title (:title post)
       :body (html/render-post post :user user)}
      (error/error 404 "Post not found"
                   "Whatever post you were looking for isn't here.  Sorry."))))

(defn tag-page
  "Tag page (posts with some tag)."
  [id & {:keys [user page-number] :or {page-number 1}}]
  (let [posts-query (q/query-> db/posts
                               :admin? user
                               :limit config/POSTS-PER-PAGE
                               :post-type "blog"
                               :offset (config/page-offset page-number))]
   (if-let [tag (oyako/fetch-one db/tags
                                 :id id
                                 :include posts-query)]
     {:title (str "Tag " (:title tag))
      :body (html/render-tag tag
                             :count (:num_posts tag)
                             :user user
                             :page-number page-number)}
     (error/error 404 "No Such Tag"
                  "Whatever tag you're looking for isn't here."))))

(defn category-page
  "Category page (posts in some category)."
  [id & {:keys [user page-number] :or {page-number 1}}]
  (let [posts-query (q/query-> db/posts
                               :admin? user
                               :limit config/POSTS-PER-PAGE
                               :post-type "blog"
                               :offset (config/page-offset page-number))]
   (if-let [cat (oyako/fetch-one db/categories
                                 :id id
                                 :include posts-query)]
     {:title (str "Category " (:title cat))
      :body (html/render-category cat
                                  :count (:num_posts cat)
                                  :user user
                                  :page-number page-number)}
     (error/error 404 "No Such Category"
                  "Whatever category you're looking for isn't here."))))

(defn archives-page-by-date []
  (let [posts (oyako/fetch-all :posts
                               :admin? false
                               :include [:category]
                               :columns [:id :title :num_comments :category_id :date_created]
                               :post-type "blog"
                               :order "date_created desc")
        by-date (reverse
                 (sort-by first
                          (group-by #(time/datestr :monthyear (:date_created %))
                                    posts)))]
    (prn (map :date_created (take 5 posts)))
    {:title "Archives"
     :body [:table
            (for [[date posts] by-date]
              (list
               [:tr [:th.big {:colspan 4} date]]
               (html/render-post-table posts)))]}))

(defn archives-page-by-comments []
  (let [posts (oyako/fetch-all :posts
                               :admin? false
                               :include [:category]
                               :post-type "blog"
                               :order "num_comments desc, date_created desc")]
    {:title "Archives"
     :body [:table
            [:tr [:th.big {:colspan 4} "Archive (most discussed)"]]
            (html/render-post-table posts)]}))

(defn tag-cloud-page []
  {:title "Tag Cloud"
   :body [:div [:h3 "Gratuitous Tag Cloud"]
          (let [tags (oyako/fetch-all :tags
                                      :order "title")
                min    10.0
                weight 9.0
                weight-fn (fn [n]
                            (if (zero? n)
                              min
                              (+ min (* weight (Math/log n)))))]
            (for [tag tags]
              [:span
               [:a {:href (link/url tag)
                    :class :cloud
                    :style (str "font-size: " (weight-fn (:num_posts tag)) "px") }
                (:title tag)]
               " "])
            )]})

(def combined-js-cache (atom nil))

(defn combined-js
  "Render Javascript files by reading them from disk and concat'ing them
  together into one blob of text.  This saves the user from needed one HTTP
  request per JS file."
  []
  (when-not @combined-js-cache
     (swap! combined-js-cache
            (fn [_]
              (apply str (mapcat #(slurp (s/join "/" [config/PUBLIC-DIR "js" (str % ".js")]))
                                 ["xregexp" "shCore"
                                  "brushes/shBrushBash"
                                  "brushes/shBrushClojure"
                                  "brushes/shBrushCss"
                                  "brushes/shBrushJava"
                                  "brushes/shBrushPerl"
                                  "brushes/shBrushPhp"
                                  "brushes/shBrushPython"
                                  "brushes/shBrushRuby"
                                  "brushes/shBrushSql"
                                  "brushes/shBrushXml"
                                  "jquery" "typewatch" "showdown" "editor"])))))
  {:headers {"Content-Type" "text/javascript;charset=UTF-8"}
   :body @combined-js-cache})


(defn do-add-comment
  "Handles POST request to add a new comment.  This is suitable for public /
   anonymous users to use."
  [ip referer post-id author email homepage markdown captcha]
  (let [spam? (not (re-matches config/CAPTCHA captcha))]
    (if-let [post (oyako/fetch-one :posts :id post-id)]
      (error/redirecting-to
       referer
       (not post)        "Tried to add a comment for a post that doesn't exist."
       (empty? ip)       "Missing IP?  That shouldn't happen."
       (empty? markdown) "You forgot to type words in your comment.  Please type some words."
       (and (not (empty? homepage))
            (try (java.net.URL. homepage) nil
                 (catch java.net.MalformedURLException _ :failed)))
       "Invalid homepage.  Please provide a URL starting with 'http[s]://'.  Or leave it blank."

       :else (try
               (oyako/insert :comments
                             {:post_id (:id post)
                              :status (if spam? "spam" "public")
                              :author (or (hiccup/escape-html author) config/DEFAULT-COMMENT-AUTHOR)
                              :email (hiccup/escape-html email)
                              :homepage (when (not (empty? homepage))
                                          (hiccup/escape-html homepage))
                              :markdown markdown
                              :ip ip})
               (db/update-counts)
               (flash/message (if spam?
                                "Comment added, awaiting moderation.
                                     (Anti-robot defense systems activated.)"
                                "Comment added.  Thanks!"))
               (response/redirect referer)

               (catch Exception e
                 (throw e)
                 (error/redirect-and-error referer
                                           "There was some kind of database error and
                                          the computer ate your comment.  Sorry.  :(")))))))
(defn sitemap-page []
  {:headers {"Content-Type" "text/xml"}
   :body (hiccup/html
          "<?xml version='1.0' encoding='UTF-8'?>"
          [:urlset {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"
                    "xmlns:xsi" "http://www.w3.org/2001/XMLSchema-instance"
                    "xsi:schemaLocation" "http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"}
           (for [post (oyako/fetch-all :posts)]
             [:url
              [:loc (str config/SITE-URL (link/url post))]
              [:lastmod (time/datestr :short (:date_created post))]
              [:changefreq "hourly"]
              [:priority 1.0]])
           (for [cat (oyako/fetch-all :categories)]
             [:url
              [:loc (str config/SITE-URL (link/url cat))]
              [:lastmod (time/datestr :short (:date_created cat))]
              [:changefreq "hourly"]
              [:priority 0.75]])
           (for [tag (oyako/fetch-all :tags)]
             [:url
              [:loc (str config/SITE-URL (link/url tag))]
              [:lastmod (time/datestr :short (:date_created tag))]
              [:changefreq "hourly"]
              [:priority 0.5]])]
          )})
