module.exports = {
  generateHtml: generateHtml
};

const moment = require('moment');
const Mustache = require('mustache');
const fs = require('fs');

function generateHtml(eNews) {
  const template = fs.readFileSync('view/enews.mst', 'utf-8');
  const partials = loadPartials();
  const model = createModel(eNews);
  return Mustache.render(template, model, partials);
};

function loadPartials() {
  return {
    title: readPartial('title.mst'),
    textItem: readPartial('text-item.mst'),
    imageItem: readPartial('image-item.mst'),
    item: readPartial('item.mst')
  }
}

function readPartial(name) {
  return fs.readFileSync(`view/partial/${name}` , 'utf-8');
}

function createModel(eNews) {
  const formattedTodaysDate = moment().format('YYYY-MM-DD');
  return {
    formattedTodaysDate: formattedTodaysDate,
    eNews: eNews
  };
}
