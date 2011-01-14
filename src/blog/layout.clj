(ns blog.layout
  "This namespace holds HTML-rendering code for the layout,
  and for minor bits of pages that had nowhere else to live."
  (:require (blog [config :as config]
                  [util :as util]
                  [db :as db]
                  [link :as link])
            (oyako [core :as oyako])
            (clojure.contrib [math :as math]
                             [string :as s])
            (sandbar [stateful-session :as session]))
  (:use (hiccup [core :only [html]]
                [page-helpers :only [link-to include-css include-js doctype]]
                [form-helpers :only [form-to submit-button label]])))

(def rss-icon
     (html
      [:span [:img.rss {:src "/img/rss.png" :alt "RSS"}] " "]))

(defn- nav-title [class x]
  [:h3 {:class class} x])

(defn- nav [user]
  (let [link-with-count (fn [x]
                          (link-to (link/url x)
                                   (str (:title x) " (" (:num_posts x) ")")))]
    (list
     (when user
       (list
        (nav-title "admin" "Admin")
        [:ul "Hello, " (:username user)
         [:li (link-to "/logout" "Log out")]
         [:li (link-to "/admin" "Control Panel")]]))

     (nav-title "categories" "Categories")
     [:ul
      (map #(vector :li (link-with-count %))
           (oyako/fetch-all db/categories
                            :where ["num_posts > 0"]
                            :order "num_posts desc"))]

     (nav-title "tags" "Tags")
     [:ul
      (map #(vector :li (link-with-count %))
           (oyako/fetch-all db/tags
                            :where ["num_posts > 0"]
                            :order "num_posts desc"))]

     (nav-title "archives" "Archives")
     [:ul
      [:li (link-to "/archives/date" "By date")]
      [:li (link-to "/archives/comments" "Most discussed")]
      [:li (link-to "/archives/tag-cloud" "Tag Cloud")]]

     (nav-title "rssfeed" "Meta")
     [:ul
      [:li (link-to "/feed" "RSS-Feeds")]])))

(defn wrap-in-layout [title body user message error]
  (html
   (doctype :xhtml-strict)
   [:html
    [:head
     [:title config/SITE-TITLE (when title (str " - " title))]
     (include-css "/css/style.css")
     (include-css "/css/shCoreEmacs.css")
     (include-css "/css/shThemeEmacs.css")
     (include-js "/js/combined.js") ;;magic; look in pages.clj
     [:link {:type "application/rss+xml" :rel "alternate" :href "/feed"}]]
    [:body
     [:div#wrapper
      [:div#main
       [:div#header
        [:h2 (link-to config/SITE-URL config/SITE-TITLE)
         [:span.additional
          config/SITE-TITLE-ADDITIONAL]]
        [:span.description config/SITE-DESCRIPTION]
        [:div#menu
         [:ul
          (map #(vector :li (link/link %))
               (oyako/fetch-all :posts
                                :columns [:id :title :url :type :status]
                                :admin? user
                                :post-type "toplevel"
                                :order "title"))]]]
       (when message [:div.message message])
       (when error [:div.error error])
       [:div#content
        [:div.content body]]]
      [:div#sponsors
       [:img {:src "/images/lisplogo_fancy_128.png"}]]
      [:div#sidebar (nav user)]

      [:div#footer
       [:div.footer-text
        "Written by " (link-to "http://jardev.net" "Yaroslav Luzin aka @jardev")
        [:br]
        "Powered by "
        (link-to "http://clojure.org" "Clojure") " and "
        (link-to "http://github.com/weavejester/compojure" "Compojure") " and "
        (link-to "http://github.com/jardev/cow-blog" "Cow-Blog")
        " originally written by " (link-to "http://briancarper.net" "Brian Carper.")
        [:br]]]]]]))

(defn preview-div []
  [:div
   [:h3 "Preview"]
   [:div#preview]])

(defn form-row [lab name el]
  [:div (label name (str lab ":"))
   [:div.form (el name)]
   [:div.clear]])

(defn submit-row [lab]
  [:div.submit
   (submit-button lab)])

(defn pagenav
  ([xs count page-number query-params]
     (let [last-page-number (math/ceil (/ count
                                          config/POSTS-PER-PAGE))
           page-range (filter #(and (> % 0) (<= % last-page-number))
                              (range (- page-number 5)
                                     (+ page-number 5)))
           f (fn [p] (s/join "&" (concat [(str "?p=" p)]
                                         (map (fn [[k v]] (str (name k) "=" v))
                                              query-params))))]
       [:div.pagenav
        [:span.navtext "Page " page-number " of " (if (zero? last-page-number) 1 last-page-number)]
        (if (> page-number 1)
          [:span.navnext
           (link-to (f 1) "&laquo; First")
           (link-to (f (dec page-number)) "&lt; Prev")])
        (for [p page-range]
          (if (= p page-number)
            [:span.num p]
            (link-to (f p) p)))
        (if (< page-number last-page-number)
          [:span.navnext
           (link-to (f (inc page-number)) "Next &raquo;")
           (link-to (f last-page-number) "Last &gt;")])])))

(defn paginate [xs page-number]
  (take config/POSTS-PER-PAGE
        (drop (* config/POSTS-PER-PAGE (dec page-number)) xs)))

(defn render-paginated [xs & {:keys [render-fn query-params count page-number]}]
  (list
   (map render-fn xs)
   (pagenav xs count page-number query-params)))

(defn status-span [x]
  (let [status (:status x)]
    [:span " [" [:span {:class status} status] "]"]))
