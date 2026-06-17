const { createListBehavior } = require('./list-behavior')

Page({
  ...createListBehavior(0),
  data: {
    ...createListBehavior(0).data,
    showEditAction: true,
    showInboundAction: false
  }
})
