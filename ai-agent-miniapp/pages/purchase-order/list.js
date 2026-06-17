const { createListBehavior } = require('./list-behavior')

Page({
  ...createListBehavior(null),
  data: {
    ...createListBehavior(null).data,
    showEditAction: false,
    showInboundAction: false
  }
})
