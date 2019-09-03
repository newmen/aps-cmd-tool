(ns aps.core
  (:require [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [cheshire.core :refer [parse-string generate-string]]
            [clj-xpath.core :refer [$x $x:text]])
  (:gen-class))

(def oa-addr
  (System/getenv "OA"))

(def oa-uri
  (str "http://" oa-addr))

(def openapi-port
  (or (System/getenv "OA_OPENAPI_PORT")
      "8440"))

(def openapi-uri
  (str oa-uri ":" openapi-port))

(def aps-token-request-body
  (slurp
   (io/resource "queries/open-api/get-aps-token.xml")))

(defn get-aps-token-response
  []
  (http/request {:url openapi-uri
                 :method :post
                 :body aps-token-request-body}))

(defn read-aps-token-response-member
  [member]
  (let [key ($x:text "./name" member)
        value ($x:text "./value/string" member)]
    [(keyword key) value]))

(defn get-aps-token-and-uri
  []
  (let [xpath "/methodResponse/params/param/value/struct/member/value/struct/member"
        promise (get-aps-token-response)
        response @promise
        body (:body response)
        members (map :node ($x xpath body))]
    (if (nil? body)
      {:error (.getMessage (:error response))}
      (into {} (map read-aps-token-response-member members)))))

(def cached-aps-token-and-uri
  (get-aps-token-and-uri))

(defn aps-controller-uri
  [path]
  (let [controller-uri (:controller_uri cached-aps-token-and-uri)]
    (str controller-uri "aps/2/" path)))

(defn aps-request-common-opts
  [method path]
  {:url (aps-controller-uri path)
   :method (keyword method)
   :insecure? true
   :headers {"APS-Token" (:aps_token cached-aps-token-and-uri)}})

(defn do-aps-request
  ([method path]
   (http/request (aps-request-common-opts method path)))
  ([method path body]
   (let [common-opts (aps-request-common-opts method path)
         json-opts (assoc-in common-opts [:headers "Content-Type"] "application/json")
         body-opts (assoc json-opts :body body)]
     (http/request body-opts))))

(defn parse-body
  [body]
  (try
    (parse-string body)
    (catch Exception e body)))

(defn highlight-aps-response
  [args]
  (let [error (:error cached-aps-token-and-uri)]
    (if error
      (.println *err* error)
      (let [promise (apply do-aps-request args)
            response @promise
            status (:status response)
            body (parse-body (:body response))
            answer {:status status :answer body}
            pretty-json (generate-string answer {:pretty true})]
        (if status
          (println pretty-json)
          (.println *err* (.getMessage (:error response))))))))

(defn run
  [args]
  (let [n (count args)]
    (if (or (= n 2) (= n 3))
      (highlight-aps-response args)
      (println "Usage: aps method path [body]"))))

(defn -main
  [& args]
  (if oa-addr
    (run args)
    (.println *err* "You must define OA environment variable with OSS host address")))
