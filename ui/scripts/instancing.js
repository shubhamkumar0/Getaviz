(function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};

/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {

/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;

/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};

/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;

/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}


/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;

/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;

/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";

/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ function(module, exports) {

	/**
	 * @author Takahiro / https://github.com/takahirox
	 */

	if (typeof AFRAME === 'undefined') {
	  throw new Error('Component attempted to register before' +
	                  'AFRAME was available.');
	}

	AFRAME.registerComponent('instancing', {
		
	    update: function () {
	    	var count = this.el.children.length;
		    var el = this.el;
	    var translateArray = new Float32Array(count*3);
	    var vectorArray = new Float32Array(count*3);
	    var colorArray = new Float32Array(count*3);
 		//shader
	    var material = new THREE.ShaderMaterial({
	      uniforms: {
	        time: {value: 0}
	      },
	      vertexShader: [
	        'attribute vec3 translate;',
	        'attribute vec3 vector;',
	        'attribute vec3 color;',
	        'uniform float time;',
	        'varying vec3 vColor;',
	        'const float g = 9.8 * 1.5;',
	        'void main() {',
	        '  vec3 offset;',
	        '  offset.xz = vector.xz * time;',
	        '  offset.y = vector.y * time - 0.5 * g * time * time;',
	        '  gl_Position = projectionMatrix * modelViewMatrix * vec4( position + translate + offset, 1.0 );',
	        '  vColor = color;',
		'}'
	      ].join('\n'),
	      fragmentShader: [
	        'varying vec3 vColor;',
	        'void main() {',
	        '  gl_FragColor = vec4( vColor, 1.0 );',
	        '}'
	      ].join('\n')
	    });
	    // console.log(this.el.children[1].getObject3D('mesh').geometry.attributes.color);
	    // console.log('babababa');
	    for(var i = 1; i < this.el.children.length; i++){
	    	var geometry = new THREE.InstancedBufferGeometry();
	    	var ele = this.el.children[i].getObject3D('mesh').geometry;
	    	geometry.copy(ele);
	    	//change impl below
	    	translateArray = ele.attributes.position.array;
	    	vectorArray = ele.attributes.normal.array;
	        // vectorArray[(i-1)*3+0] = (Math.random() - 0.5) * 100.0;
	        // vectorArray[(i-1)*3+1] = (Math.random() + 1.5) * 100.0;
	        // vectorArray[(i-1)*3+2] = (Math.random() - 0.5) * 100.0;
	        colorArray = ele.attributes.color.array;
	        geometry.addAttribute('translate', new THREE.InstancedBufferAttribute(translateArray, 3, true));
	    	// geometry.addAttribute('vector', new THREE.InstancedBufferAttribute(vectorArray, 3, 1));
	    	geometry.addAttribute('color', new THREE.InstancedBufferAttribute(colorArray, 3, true));
	    	var mesh = new THREE.Mesh(geometry, material);

		    this.model = mesh;
		    el.setObject3D('mesh', mesh);
		    el.emit('model-loaded', {format:'mesh', model: mesh});
		  
	    }

	     }
	  	
	});

	}
/******/ ]);
