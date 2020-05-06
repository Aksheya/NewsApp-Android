var express = require("express");
var app = express();
var cors = require('cors');
const axios = require('axios');

const gdapikey = '4a39b499-fbc5-440e-bb6d-a655fc931dc4';
const default_gd_image = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
const gd_base_URL = "https://content.guardianapis.com/";
const googleTrends = require('google-trends-api');
app.use(cors());

getFragmentData = (path) => {
    return new Promise((resolve, reject) => {
        axios.get(gd_base_URL + path + "&show-blocks=all")
            .then(response => {
                if (response.data && response.data.response && response.data.response.results) {
                    var results = response.data.response.results;
                    var home_news = checkParamsGD(results);
                    resolve(home_news);
                }
            })
            .catch(error => {
                reject(error);
            })
    });
}


checkDetailsGD = (content) => {
    var params = ["webTitle", "webPublicationDate", "webUrl", "id", "sectionName"];
    var count = 0;
    var article = {};
    if (content == undefined) return article;
    params.forEach(param => {
        if (content[param] && content[param] != "" && content[param] != "null")
            count += 1;
    });
    if (count == 5 && content.blocks && content.blocks["body"]) {
        str = "";
        for (var i = 0; i < content.blocks.body.length; i++) {
            summary = content.blocks.body[i];
            if (summary !== undefined && summary.bodyHtml !== undefined && summary.bodyHtml !== "" && summary.bodyHtml != "null") {
                str += summary.bodyHtml;
            }
        }
        article.time = content.webPublicationDate;
        article.title = content.webTitle;
        article.shareUrl = content.webUrl;
        article.description = str;
        article.articleId = content.id;
        article.section = content.sectionName;
        result = content;
        if (result.blocks && result.blocks.main && result.blocks.main.elements && result.blocks.main.elements[0] && result.blocks.main.elements[0].assets &&
            result.blocks.main.elements[0].assets[0] && result.blocks.main.elements[0].assets[0].file
            && result.blocks.main.elements[0].assets[0].file !== "" && result.blocks.main.elements[0].assets[0].file !== "null") {
            article.image = result.blocks.main.elements[0].assets[0].file;
        }
        else {
            article.image = default_gd_image;
        }

    }
    return article;
}

checkParamsGD = (results) => {
    var home_news = []
    var params = ["webTitle", "webPublicationDate", "sectionName", "webUrl", "id"]
    for (var i = 0; i < results.length; i++) {
        var article = {}
        var result = results[i];
        var count = 0;
        params.forEach(param => {
            if (result[param] && result[param] != "null" && result[param] != "")
                count += 1
        });
        if (count == 5) {
            article.section = result.sectionName;
            article.time = result.webPublicationDate;
            article.title = result.webTitle;
            article.articleId = result.id;
            article.shareUrl = result.webUrl;
            if (result.blocks && result.blocks.main && result.blocks.main.elements && result.blocks.main.elements[0] && result.blocks.main.elements[0].assets &&
                result.blocks.main.elements[0].assets[0] && result.blocks.main.elements[0].assets[0].file
                && result.blocks.main.elements[0].assets[0].file != "" && result.blocks.main.elements[0].assets[0].file != "null") {
                article.image = result.blocks.main.elements[0].assets[0].file;
            }
            else {
                article.image = default_gd_image;
            }
            home_news.push(article);
            if (home_news.length === 10) break;
        }
    }
    return home_news;
}

app.get("/", (req, res) => {
    res.json();
});





app.get("/detail_article", (req, res) => {
    articleId = req.query.articleId;
    axios.get(gd_base_URL + articleId + "?api-key=" + gdapikey + "&show-blocks=all")
        .then(response => {
            if (response.data && response.data.response && response.data.response.content) {
                var content = response.data.response.content;
                results = checkDetailsGD(content);
                res.send(results)
            }
        })
        .catch(error => {
            console.log(error);
        });
});

app.get("/search", async (req, res) => {
    keyword = req.query.keyword;
    var news = [];
    try {
        news = await getFragmentData("search?q=" + keyword + '&api-key=' + gdapikey, "section");
        res.send(news)
    }
    catch (error) {
        console.log(error);
        res.send(news)
    }
});

app.get("/home_fragment", (req, res) => {
    try {
        axios.get(gd_base_URL + "search?order-by=newest&show-fields=starRating,headline,thumbnail,short-url&api-key=" + gdapikey)
            .then(response => {
                var home_news = []
                if (response.data && response.data.response && response.data.response.results) {
                    var params = ["webTitle", "sectionName", "webPublicationDate", "id", "webUrl"]
                    var results = response.data.response.results;
                    for (var i = 0; i < results.length; i++) {
                        var article = {}
                        var result = results[i];
                        var count = 0;
                        params.forEach(param => {
                            if (result[param] && result[param] != "null" && result[param] != "")
                                count += 1
                        });
                        if (count == 5) {
                            article.section = result.sectionName;
                            article.time = result.webPublicationDate;
                            article.title = result.webTitle;
                            article.articleId = result.id;
                            article.shareUrl = result.webUrl;
                            if (result.fields && result.fields["thumbnail"]) {
                                article.image = result.fields.thumbnail;
                            } else {
                                article.image = default_gd_image;
                            }
                            home_news.push(article);
                            if (home_news.length === 10) break;
                        }
                    }
                }
                res.send(home_news)
            })
            .catch(error => {
                console.log(error);
            });

    }
    catch (error) {
        console.log(error)
    }
});


app.get("/world_fragment", async (req, res) => {
    try {
        var news = await getFragmentData("world?api-key=" + gdapikey);
        res.send(news)
    }
    catch (error) {
        console.log(error)
    }
});

app.get("/business_fragment", async (req, res) => {
    try {
        var news = await getFragmentData("business?api-key=" + gdapikey);
        res.send(news)
    }
    catch (error) {
        console.log(error)
    }
});

app.get("/politics_fragment", async (req, res) => {
    try {
        var news = await getFragmentData("politics?api-key=" + gdapikey);
        res.send(news)
    }
    catch (error) {
        console.log(error)
    }
});

app.get("/sports_fragment", async (req, res) => {
    try {
        var news = await getFragmentData("sport?api-key=" + gdapikey);
        res.send(news)
    }
    catch (error) {
        console.log(error)
    }
});

app.get("/technology_fragment", async (req, res) => {
    try {
        var news = await getFragmentData("technology?api-key=" + gdapikey);
        res.send(news)
    }
    catch (error) {
        console.log(error)
    }
});

app.get("/science_fragment", async (req, res) => {
    try {
        var news = await getFragmentData("science?api-key=" + gdapikey);
        res.send(news)
    }
    catch (error) {
        console.log(error)
    }
});

app.get("/google_trends", (req, res) => {
    keyword = req.query.keyword;
    try {
        if (keyword === "" || keyword === undefined)
            keyword = "coronavirus";
        options = {
            keyword: keyword,
            startTime: new Date('2019-06-01')
        }
        googleTrends.interestOverTime(options)
            .then(function (results) {
                res.send(results);
            })
            .catch(function (error) {
                console.log(error);
            })
    }
    catch (e) {
        console.log(e)
    }

});


const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

