;; Patch daemon users for lein-daemon
(in-ns 'leiningen.core)

(alter-var-root #'project
                (fn [p]
                  (merge p
                         {:daemon
                          (when-let [daemon (:daemon p)]
                            (let [user (System/getProperty "user.name")]
                              (reduce (fn [res d]
                                        (assoc res
                                          (first d)
                                          (merge (second d)
                                                 {:options (merge (:options (second d))
                                                                  {:user user})})))
                                     {}
                                     daemon)))})))
