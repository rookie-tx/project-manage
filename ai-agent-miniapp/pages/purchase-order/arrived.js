const { createListBehavior } = require('./list-behavior')

Page({
  ...createListBehavior(2),
  data: {
    ...createListBehavior(2).data,
    showEditAction: false,
    showInboundAction: false
  }
})
