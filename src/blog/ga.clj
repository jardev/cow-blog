(ns blog.ga
  "Contains functions to generate Google Analytics JS-Script"
  (:require (blog [config :as config])))


(def GA-SCRIPT "
<script type=\"text/javascript\">
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '%s']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
")

(defn ga []
  (when config/GA-ACCOUNT
    (format GA-SCRIPT config/GA-ACCOUNT)))
